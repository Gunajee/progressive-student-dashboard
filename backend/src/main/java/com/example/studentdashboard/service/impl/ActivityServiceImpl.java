package com.example.studentdashboard.service.impl;

import com.example.studentdashboard.dto.ActivityEventDto;
import com.example.studentdashboard.entity.ActivityEvent;
import com.example.studentdashboard.entity.ActivityEventType;
import com.example.studentdashboard.entity.Course;
import com.example.studentdashboard.entity.Lesson;
import com.example.studentdashboard.entity.User;
import com.example.studentdashboard.repository.ActivityEventRepository;
import com.example.studentdashboard.repository.CourseRepository;
import com.example.studentdashboard.repository.LessonRepository;
import com.example.studentdashboard.repository.UserRepository;
import com.example.studentdashboard.service.ActivityService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ActivityServiceImpl implements ActivityService {

    private final ActivityEventRepository activityEventRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final LessonRepository lessonRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<ActivityEventDto> getStudentActivity(Long studentId, Pageable pageable) {
        Page<ActivityEvent> events = activityEventRepository.findByStudentIdOrderByEventDateDesc(studentId, pageable);
        return events.map(this::mapToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ActivityEventDto> getRecentActivity(Long studentId, int limit) {
        Page<ActivityEventDto> page = getStudentActivity(studentId, PageRequest.of(0, limit));
        return page.getContent();
    }

    @Override
    @Transactional
    public void logActivity(Long studentId, Long courseId, Long lessonId, ActivityEventType eventType, int durationMinutes, String metadata) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));

        Course course = null;
        if (courseId != null) {
            course = courseRepository.findById(courseId).orElse(null);
        }

        Lesson lesson = null;
        if (lessonId != null) {
            lesson = lessonRepository.findById(lessonId).orElse(null);
        }

        ActivityEvent event = ActivityEvent.builder()
                .student(student)
                .course(course)
                .lesson(lesson)
                .eventType(eventType)
                .durationMinutes(durationMinutes)
                .metadata(metadata)
                .build();

        activityEventRepository.save(event);
    }

    @Override
    @Transactional(readOnly = true)
    public int calculateStreak(Long studentId) {
        List<ActivityEvent> events = activityEventRepository.findByStudentIdOrderByEventDateDesc(studentId);
        
        // Extract distinct local dates of events
        List<LocalDate> activeDates = events.stream()
                .map(event -> event.getEventDate().toLocalDate())
                .distinct()
                .collect(Collectors.toList());

        if (activeDates.isEmpty()) {
            return 0;
        }

        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        LocalDate mostRecent = activeDates.get(0);

        // If the student hasn't been active today or yesterday, streak is 0
        if (!mostRecent.equals(today) && !mostRecent.equals(yesterday)) {
            return 0;
        }

        int streak = 1;
        LocalDate current = mostRecent;

        for (int i = 1; i < activeDates.size(); i++) {
            LocalDate nextDate = activeDates.get(i);
            if (nextDate.equals(current.minusDays(1))) {
                streak++;
                current = nextDate;
            } else {
                break;
            }
        }

        return streak;
    }

    private ActivityEventDto mapToDto(ActivityEvent event) {
        return ActivityEventDto.builder()
                .id(event.getId())
                .studentName(event.getStudent().getName())
                .courseId(event.getCourse() != null ? event.getCourse().getId() : null)
                .courseTitle(event.getCourse() != null ? event.getCourse().getTitle() : null)
                .lessonId(event.getLesson() != null ? event.getLesson().getId() : null)
                .lessonTitle(event.getLesson() != null ? event.getLesson().getTitle() : null)
                .eventType(event.getEventType())
                .durationMinutes(event.getDurationMinutes())
                .eventDate(event.getEventDate())
                .metadata(event.getMetadata())
                .build();
    }
}

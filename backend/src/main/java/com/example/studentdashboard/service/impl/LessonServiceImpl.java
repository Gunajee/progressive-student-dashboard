package com.example.studentdashboard.service.impl;

import com.example.studentdashboard.dto.LessonDto;
import com.example.studentdashboard.entity.Lesson;
import com.example.studentdashboard.entity.LessonProgress;
import com.example.studentdashboard.entity.LessonProgressStatus;
import com.example.studentdashboard.repository.EnrollmentRepository;
import com.example.studentdashboard.repository.LessonProgressRepository;
import com.example.studentdashboard.repository.LessonRepository;
import com.example.studentdashboard.service.LessonService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LessonServiceImpl implements LessonService {

    private final EnrollmentRepository enrollmentRepository;
    private final LessonRepository lessonRepository;
    private final LessonProgressRepository lessonProgressRepository;

    @Override
    @Transactional(readOnly = true)
    public List<LessonDto> getLessonsForCourse(Long studentId, Long courseId) {
        // Verify student is enrolled in the course
        enrollmentRepository.findByStudentIdAndCourseId(studentId, courseId)
                .orElseThrow(() -> new IllegalArgumentException("Student is not enrolled in this course"));

        List<Lesson> lessons = lessonRepository.findByCourseIdOrderByOrderIndexAsc(courseId);
        return lessons.stream()
                .map(lesson -> mapToLessonDto(lesson, studentId))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public LessonDto getLessonDetails(Long studentId, Long lessonId) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new IllegalArgumentException("Lesson not found"));

        // Verify student is enrolled in the lesson's course
        enrollmentRepository.findByStudentIdAndCourseId(studentId, lesson.getCourse().getId())
                .orElseThrow(() -> new IllegalArgumentException("Student is not enrolled in this course"));

        return mapToLessonDto(lesson, studentId);
    }

    private LessonDto mapToLessonDto(Lesson lesson, Long studentId) {
        Optional<LessonProgress> progressOpt = lessonProgressRepository.findByStudentIdAndLessonId(studentId, lesson.getId());

        LessonProgressStatus status = progressOpt.map(LessonProgress::getStatus).orElse(LessonProgressStatus.NOT_STARTED);
        int timeSpent = progressOpt.map(LessonProgress::getTimeSpentMinutes).orElse(0);

        return LessonDto.builder()
                .id(lesson.getId())
                .courseId(lesson.getCourse().getId())
                .title(lesson.getTitle())
                .description(lesson.getDescription())
                .orderIndex(lesson.getOrderIndex())
                .estimatedMinutes(lesson.getEstimatedMinutes())
                .status(status)
                .timeSpentMinutes(timeSpent)
                .build();
    }
}

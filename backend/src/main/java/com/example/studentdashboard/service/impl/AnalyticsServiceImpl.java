package com.example.studentdashboard.service.impl;

import com.example.studentdashboard.dto.*;
import com.example.studentdashboard.entity.*;
import com.example.studentdashboard.repository.*;
import com.example.studentdashboard.service.AnalyticsService;
import com.example.studentdashboard.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalyticsServiceImpl implements AnalyticsService {

    private final ActivityEventRepository activityEventRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final LessonRepository lessonRepository;
    private final LessonProgressRepository lessonProgressRepository;
    private final CourseService courseService;

    @Override
    @Transactional(readOnly = true)
    public LearningTrendDto getLearningTrend(Long studentId, String range) {
        int days = "30d".equalsIgnoreCase(range) ? 30 : 7;
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days - 1);

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay();

        List<ActivityEvent> events = activityEventRepository.findByStudentIdAndEventDateBetween(studentId, startDateTime, endDateTime);

        // Group by local date and sum minutes
        Map<LocalDate, Integer> dateToMinutes = events.stream()
                .collect(Collectors.groupingBy(
                        event -> event.getEventDate().toLocalDate(),
                        Collectors.summingInt(ActivityEvent::getDurationMinutes)
                ));

        List<LearningTrendPointDto> dataPoints = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        for (int i = 0; i < days; i++) {
            LocalDate date = startDate.plusDays(i);
            int minutes = dateToMinutes.getOrDefault(date, 0);
            dataPoints.add(new LearningTrendPointDto(date.format(formatter), minutes));
        }

        return LearningTrendDto.builder()
                .range(days + "d")
                .data(dataPoints)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public CourseDistributionDto getCourseDistribution(Long studentId) {
        List<CourseDto> courses = courseService.getEnrolledCourses(studentId);

        int completed = 0;
        int inProgress = 0;
        int notStarted = 0;

        for (CourseDto course : courses) {
            double progress = course.getProgressPercentage();
            if (progress >= 100.0) {
                completed++;
            } else if (progress > 0.0) {
                inProgress++;
            } else {
                notStarted++;
            }
        }

        return CourseDistributionDto.builder()
                .completed(completed)
                .inProgress(inProgress)
                .notStarted(notStarted)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseAnalyticsDto> getCourseAnalyticsList(Long studentId) {
        List<Enrollment> enrollments = enrollmentRepository.findByStudentId(studentId);

        return enrollments.stream().map(enrollment -> {
            Course course = enrollment.getCourse();
            int totalLessons = lessonRepository.findByCourseIdOrderByOrderIndexAsc(course.getId()).size();

            List<LessonProgress> progresses = lessonProgressRepository.findByStudentIdAndLessonCourseId(studentId, course.getId());
            long completedLessons = progresses.stream()
                    .filter(p -> p.getStatus() == LessonProgressStatus.COMPLETED)
                    .count();

            int timeSpentMinutes = progresses.stream()
                    .mapToInt(LessonProgress::getTimeSpentMinutes)
                    .sum();

            double progressPercentage = courseService.calculateCourseProgress(studentId, course.getId());
            int remainingLessons = totalLessons - (int) completedLessons;

            return CourseAnalyticsDto.builder()
                    .courseTitle(course.getTitle())
                    .totalLessons(totalLessons)
                    .completedLessons((int) completedLessons)
                    .progressPercentage(progressPercentage)
                    .timeSpentMinutes(timeSpentMinutes)
                    .remainingLessons(remainingLessons)
                    .build();
        }).collect(Collectors.toList());
    }
}

package com.example.studentdashboard.service.impl;

import com.example.studentdashboard.dto.*;
import com.example.studentdashboard.entity.*;
import com.example.studentdashboard.repository.ActivityEventRepository;
import com.example.studentdashboard.repository.LessonProgressRepository;
import com.example.studentdashboard.service.ActivityService;
import com.example.studentdashboard.service.CourseService;
import com.example.studentdashboard.service.RecommendationService;
import com.example.studentdashboard.service.StudentDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StudentDashboardServiceImpl implements StudentDashboardService {

    private final CourseService courseService;
    private final ActivityService activityService;
    private final RecommendationService recommendationService;
    private final LessonProgressRepository lessonProgressRepository;
    private final ActivityEventRepository activityEventRepository;

    @Override
    @Transactional(readOnly = true)
    public DashboardDto getDashboard(Long studentId) {
        List<CourseDto> enrolledCourses = courseService.getEnrolledCourses(studentId);

        long completedLessonsCount = lessonProgressRepository.findByStudentId(studentId).stream()
                .filter(p -> p.getStatus() == LessonProgressStatus.COMPLETED)
                .count();

        int totalLearningMinutes = activityEventRepository.findByStudentIdOrderByEventDateDesc(studentId).stream()
                .mapToInt(ActivityEvent::getDurationMinutes)
                .sum();

        double averageProgress = enrolledCourses.stream()
                .mapToDouble(CourseDto::getProgressPercentage)
                .average()
                .orElse(0.0);
        double overallProgress = Math.round(averageProgress * 10.0) / 10.0;

        int currentStreak = activityService.calculateStreak(studentId);
        List<ActivityEventDto> recentActivity = activityService.getRecentActivity(studentId, 5);
        List<RecommendationDto> recommendations = recommendationService.getRecommendations(studentId);

        return DashboardDto.builder()
                .totalCourses(enrolledCourses.size())
                .completedLessons((int) completedLessonsCount)
                .totalLearningMinutes(totalLearningMinutes)
                .overallProgress(overallProgress)
                .currentStreak(currentStreak)
                .recentActivity(recentActivity)
                .recommendations(recommendations)
                .courseProgress(enrolledCourses)
                .build();
    }
}

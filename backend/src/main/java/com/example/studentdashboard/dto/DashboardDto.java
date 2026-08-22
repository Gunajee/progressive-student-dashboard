package com.example.studentdashboard.dto;

import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardDto {
    private Integer totalCourses;
    private Integer completedLessons;
    private Integer totalLearningMinutes;
    private Double overallProgress;
    private Integer currentStreak;
    private List<ActivityEventDto> recentActivity;
    private List<RecommendationDto> recommendations;
    private List<CourseDto> courseProgress;
}

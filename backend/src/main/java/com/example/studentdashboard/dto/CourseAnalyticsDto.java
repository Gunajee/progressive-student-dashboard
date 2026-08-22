package com.example.studentdashboard.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseAnalyticsDto {
    private String courseTitle;
    private Integer totalLessons;
    private Integer completedLessons;
    private Double progressPercentage;
    private Integer timeSpentMinutes;
    private Integer remainingLessons;
}

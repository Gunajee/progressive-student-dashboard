package com.example.studentdashboard.service;

import com.example.studentdashboard.dto.CourseAnalyticsDto;
import com.example.studentdashboard.dto.CourseDistributionDto;
import com.example.studentdashboard.dto.LearningTrendDto;

import java.util.List;

public interface AnalyticsService {
    LearningTrendDto getLearningTrend(Long studentId, String range);
    CourseDistributionDto getCourseDistribution(Long studentId);
    List<CourseAnalyticsDto> getCourseAnalyticsList(Long studentId);
}

package com.example.studentdashboard.controller;

import com.example.studentdashboard.dto.CourseAnalyticsDto;
import com.example.studentdashboard.dto.CourseDistributionDto;
import com.example.studentdashboard.dto.LearningTrendDto;
import com.example.studentdashboard.entity.User;
import com.example.studentdashboard.repository.UserRepository;
import com.example.studentdashboard.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/student/analytics")
@RequiredArgsConstructor
public class StudentAnalyticsController {

    private final AnalyticsService analyticsService;
    private final UserRepository userRepository;

    private Long getAuthenticatedStudentId(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found"))
                .getId();
    }

    @GetMapping("/learning-trend")
    public ResponseEntity<LearningTrendDto> getLearningTrend(
            @RequestParam(defaultValue = "7d") String range,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Long studentId = getAuthenticatedStudentId(userDetails);
        return ResponseEntity.ok(analyticsService.getLearningTrend(studentId, range));
    }

    @GetMapping("/course-distribution")
    public ResponseEntity<CourseDistributionDto> getCourseDistribution(@AuthenticationPrincipal UserDetails userDetails) {
        Long studentId = getAuthenticatedStudentId(userDetails);
        return ResponseEntity.ok(analyticsService.getCourseDistribution(studentId));
    }

    @GetMapping("/course-progress")
    public ResponseEntity<List<CourseAnalyticsDto>> getCourseProgress(@AuthenticationPrincipal UserDetails userDetails) {
        Long studentId = getAuthenticatedStudentId(userDetails);
        return ResponseEntity.ok(analyticsService.getCourseAnalyticsList(studentId));
    }
}

package com.example.studentdashboard.controller;

import com.example.studentdashboard.dto.RecommendationDto;
import com.example.studentdashboard.repository.UserRepository;
import com.example.studentdashboard.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/student/recommendations")
@RequiredArgsConstructor
public class StudentRecommendationController {

    private final RecommendationService recommendationService;
    private final UserRepository userRepository;

    private Long getAuthenticatedStudentId(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found"))
                .getId();
    }

    @GetMapping
    public ResponseEntity<List<RecommendationDto>> getRecommendations(@AuthenticationPrincipal UserDetails userDetails) {
        Long studentId = getAuthenticatedStudentId(userDetails);
        return ResponseEntity.ok(recommendationService.getRecommendations(studentId));
    }
}

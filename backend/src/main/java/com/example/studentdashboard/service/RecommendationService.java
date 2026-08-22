package com.example.studentdashboard.service;

import com.example.studentdashboard.dto.RecommendationDto;

import java.util.List;

public interface RecommendationService {
    List<RecommendationDto> getRecommendations(Long studentId);
}

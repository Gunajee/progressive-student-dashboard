package com.example.studentdashboard.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecommendationDto {
    private String id;
    private String type;
    private String title;
    private String description;
    private String priority; // HIGH, MEDIUM, LOW
    private Long courseId;
    private Long lessonId;
    private String reason;
}

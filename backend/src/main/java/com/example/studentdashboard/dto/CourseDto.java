package com.example.studentdashboard.dto;

import com.example.studentdashboard.entity.Difficulty;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseDto {
    private Long id;
    private String title;
    private String description;
    private String category;
    private Difficulty difficulty;
    private Integer estimatedHours;
    private Double progressPercentage;
}

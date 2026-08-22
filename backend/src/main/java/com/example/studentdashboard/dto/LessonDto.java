package com.example.studentdashboard.dto;

import com.example.studentdashboard.entity.LessonProgressStatus;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LessonDto {
    private Long id;
    private Long courseId;
    private String title;
    private String description;
    private Integer orderIndex;
    private Integer estimatedMinutes;
    private LessonProgressStatus status;
    private Integer timeSpentMinutes;
}

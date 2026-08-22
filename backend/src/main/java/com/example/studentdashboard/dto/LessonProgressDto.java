package com.example.studentdashboard.dto;

import com.example.studentdashboard.entity.LessonProgressStatus;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LessonProgressDto {
    private Long id;
    private Long lessonId;
    private String lessonTitle;
    private LessonProgressStatus status;
    private LocalDateTime completedAt;
    private Integer timeSpentMinutes;
}

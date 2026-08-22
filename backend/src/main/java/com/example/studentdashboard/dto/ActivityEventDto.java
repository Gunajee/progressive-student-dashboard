package com.example.studentdashboard.dto;

import com.example.studentdashboard.entity.ActivityEventType;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityEventDto {
    private Long id;
    private String studentName;
    private Long courseId;
    private String courseTitle;
    private Long lessonId;
    private String lessonTitle;
    private ActivityEventType eventType;
    private Integer durationMinutes;
    private LocalDateTime eventDate;
    private String metadata;
}

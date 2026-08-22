package com.example.studentdashboard.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MentorStudentSummaryDto {
    private UserDto student;
    private Integer courseCount;
    private Double overallProgress;
    private Integer learningMinutes;
    private LocalDateTime lastActive;
    private StudentStatus status;
}

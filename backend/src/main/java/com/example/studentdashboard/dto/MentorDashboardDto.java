package com.example.studentdashboard.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MentorDashboardDto {
    private Integer totalStudents;
    private Integer activeStudents;
    private Double averageProgress;
    private Integer atRiskStudents;
}

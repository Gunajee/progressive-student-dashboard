package com.example.studentdashboard.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LearningTrendPointDto {
    private String date;
    private Integer minutes;
}

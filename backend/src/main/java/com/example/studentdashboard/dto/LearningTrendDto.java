package com.example.studentdashboard.dto;

import lombok.*;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LearningTrendDto {
    private String range;
    private List<LearningTrendPointDto> data;
}

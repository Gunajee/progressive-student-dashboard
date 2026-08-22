package com.example.studentdashboard.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseDistributionDto {
    private Integer completed;
    private Integer inProgress;
    private Integer notStarted;
}

package com.example.studentdashboard.dto;

import com.example.studentdashboard.entity.LessonProgressStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProgressUpdateDto {

    @NotNull(message = "Status is required")
    private LessonProgressStatus status;

    @NotNull(message = "Time spent is required")
    @Min(value = 0, message = "Time spent must be non-negative")
    private Integer timeSpentMinutes;
}

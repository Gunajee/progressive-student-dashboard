package com.example.studentdashboard.dto;

import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentDetailsDto {
    private UserDto student;
    private Integer courseCount;
    private Double overallProgress;
    private Integer learningMinutes;
    private StudentStatus status;
    private Integer currentStreak;
    private List<CourseDto> courses;
}

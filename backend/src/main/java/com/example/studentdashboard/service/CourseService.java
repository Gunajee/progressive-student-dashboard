package com.example.studentdashboard.service;

import com.example.studentdashboard.dto.CourseDto;

import java.util.List;

public interface CourseService {
    List<CourseDto> getEnrolledCourses(Long studentId);
    CourseDto getEnrolledCourseDetail(Long studentId, Long courseId);
    double calculateCourseProgress(Long studentId, Long courseId);
}

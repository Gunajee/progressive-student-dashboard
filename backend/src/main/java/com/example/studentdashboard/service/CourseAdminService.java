package com.example.studentdashboard.service;

import com.example.studentdashboard.dto.CourseDto;
import java.util.List;

public interface CourseAdminService {
    List<CourseDto> getAllCourses();
    CourseDto getCourseById(Long courseId);
    CourseDto createCourse(CourseDto courseDto);
    CourseDto updateCourse(Long courseId, CourseDto courseDto);
    void deleteCourse(Long courseId);
}

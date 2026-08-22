package com.example.studentdashboard.service.impl;

import com.example.studentdashboard.dto.CourseDto;
import com.example.studentdashboard.entity.Course;
import com.example.studentdashboard.repository.CourseRepository;
import com.example.studentdashboard.service.CourseAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseAdminServiceImpl implements CourseAdminService {

    private final CourseRepository courseRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CourseDto> getAllCourses() {
        return courseRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CourseDto getCourseById(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found"));
        return mapToDto(course);
    }

    @Override
    @Transactional
    public CourseDto createCourse(CourseDto courseDto) {
        Course course = Course.builder()
                .title(courseDto.getTitle())
                .description(courseDto.getDescription())
                .category(courseDto.getCategory())
                .difficulty(courseDto.getDifficulty())
                .estimatedHours(courseDto.getEstimatedHours())
                .build();
        return mapToDto(courseRepository.save(course));
    }

    @Override
    @Transactional
    public CourseDto updateCourse(Long courseId, CourseDto courseDto) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found"));
        
        course.setTitle(courseDto.getTitle());
        course.setDescription(courseDto.getDescription());
        course.setCategory(courseDto.getCategory());
        course.setDifficulty(courseDto.getDifficulty());
        course.setEstimatedHours(courseDto.getEstimatedHours());
        
        return mapToDto(courseRepository.save(course));
    }

    @Override
    @Transactional
    public void deleteCourse(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found"));
        courseRepository.delete(course);
    }

    private CourseDto mapToDto(Course course) {
        return CourseDto.builder()
                .id(course.getId())
                .title(course.getTitle())
                .description(course.getDescription())
                .category(course.getCategory())
                .difficulty(course.getDifficulty())
                .estimatedHours(course.getEstimatedHours())
                .build();
    }
}

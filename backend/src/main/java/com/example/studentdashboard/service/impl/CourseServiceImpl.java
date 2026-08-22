package com.example.studentdashboard.service.impl;

import com.example.studentdashboard.dto.CourseDto;
import com.example.studentdashboard.entity.Course;
import com.example.studentdashboard.entity.Enrollment;
import com.example.studentdashboard.entity.LessonProgressStatus;
import com.example.studentdashboard.repository.CourseRepository;
import com.example.studentdashboard.repository.EnrollmentRepository;
import com.example.studentdashboard.repository.LessonProgressRepository;
import com.example.studentdashboard.repository.LessonRepository;
import com.example.studentdashboard.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {

    private final EnrollmentRepository enrollmentRepository;
    private final LessonRepository lessonRepository;
    private final LessonProgressRepository lessonProgressRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CourseDto> getEnrolledCourses(Long studentId) {
        List<Enrollment> enrollments = enrollmentRepository.findByStudentId(studentId);
        return enrollments.stream()
                .map(enrollment -> mapToCourseDto(enrollment.getCourse(), studentId))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CourseDto getEnrolledCourseDetail(Long studentId, Long courseId) {
        enrollmentRepository.findByStudentIdAndCourseId(studentId, courseId)
                .orElseThrow(() -> new IllegalArgumentException("Student not enrolled in this course or course not found"));

        List<Enrollment> enrollments = enrollmentRepository.findByStudentId(studentId);
        Course course = enrollments.stream()
                .map(Enrollment::getCourse)
                .filter(c -> c.getId().equals(courseId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Course not found"));

        return mapToCourseDto(course, studentId);
    }

    @Override
    @Transactional(readOnly = true)
    public double calculateCourseProgress(Long studentId, Long courseId) {
        int totalLessons = lessonRepository.findByCourseIdOrderByOrderIndexAsc(courseId).size();
        if (totalLessons == 0) {
            return 0.0;
        }

        long completedLessons = lessonProgressRepository.findByStudentIdAndLessonCourseId(studentId, courseId).stream()
                .filter(p -> p.getStatus() == LessonProgressStatus.COMPLETED)
                .count();

        double progress = ((double) completedLessons / totalLessons) * 100.0;
        return Math.round(progress * 10.0) / 10.0; // round to 1 decimal place
    }

    private CourseDto mapToCourseDto(Course course, Long studentId) {
        return CourseDto.builder()
                .id(course.getId())
                .title(course.getTitle())
                .description(course.getDescription())
                .category(course.getCategory())
                .difficulty(course.getDifficulty())
                .estimatedHours(course.getEstimatedHours())
                .progressPercentage(calculateCourseProgress(studentId, course.getId()))
                .build();
    }
}

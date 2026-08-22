package com.example.studentdashboard.controller;

import com.example.studentdashboard.dto.CourseDto;
import com.example.studentdashboard.dto.LessonDto;
import com.example.studentdashboard.entity.User;
import com.example.studentdashboard.repository.UserRepository;
import com.example.studentdashboard.service.CourseService;
import com.example.studentdashboard.service.LessonService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/student")
@RequiredArgsConstructor
public class StudentCourseController {

    private final CourseService courseService;
    private final LessonService lessonService;
    private final UserRepository userRepository;

    private Long getAuthenticatedStudentId(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found"))
                .getId();
    }

    @GetMapping("/courses")
    public ResponseEntity<List<CourseDto>> getEnrolledCourses(@AuthenticationPrincipal UserDetails userDetails) {
        Long studentId = getAuthenticatedStudentId(userDetails);
        return ResponseEntity.ok(courseService.getEnrolledCourses(studentId));
    }

    @GetMapping("/courses/{courseId}")
    public ResponseEntity<CourseDto> getCourseDetail(
            @PathVariable Long courseId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Long studentId = getAuthenticatedStudentId(userDetails);
        return ResponseEntity.ok(courseService.getEnrolledCourseDetail(studentId, courseId));
    }

    @GetMapping("/courses/{courseId}/lessons")
    public ResponseEntity<List<LessonDto>> getCourseLessons(
            @PathVariable Long courseId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Long studentId = getAuthenticatedStudentId(userDetails);
        return ResponseEntity.ok(lessonService.getLessonsForCourse(studentId, courseId));
    }

    @GetMapping("/lessons/{lessonId}")
    public ResponseEntity<LessonDto> getLessonDetail(
            @PathVariable Long lessonId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Long studentId = getAuthenticatedStudentId(userDetails);
        return ResponseEntity.ok(lessonService.getLessonDetails(studentId, lessonId));
    }
}

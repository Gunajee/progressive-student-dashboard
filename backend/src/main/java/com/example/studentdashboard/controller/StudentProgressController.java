package com.example.studentdashboard.controller;

import com.example.studentdashboard.dto.LessonProgressDto;
import com.example.studentdashboard.dto.ProgressUpdateDto;
import com.example.studentdashboard.repository.UserRepository;
import com.example.studentdashboard.service.ProgressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/student")
@RequiredArgsConstructor
public class StudentProgressController {

    private final ProgressService progressService;
    private final UserRepository userRepository;

    private Long getAuthenticatedStudentId(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found"))
                .getId();
    }

    @GetMapping("/progress")
    public ResponseEntity<List<LessonProgressDto>> getProgress(@AuthenticationPrincipal UserDetails userDetails) {
        Long studentId = getAuthenticatedStudentId(userDetails);
        return ResponseEntity.ok(progressService.getStudentProgress(studentId));
    }

    @GetMapping("/courses/{courseId}/progress")
    public ResponseEntity<List<LessonProgressDto>> getCourseProgress(
            @PathVariable Long courseId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Long studentId = getAuthenticatedStudentId(userDetails);
        return ResponseEntity.ok(progressService.getCourseProgress(studentId, courseId));
    }

    @PutMapping("/lessons/{lessonId}/progress")
    public ResponseEntity<LessonProgressDto> updateLessonProgress(
            @PathVariable Long lessonId,
            @Valid @RequestBody ProgressUpdateDto updateDto,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Long studentId = getAuthenticatedStudentId(userDetails);
        return ResponseEntity.ok(progressService.updateLessonProgress(studentId, lessonId, updateDto));
    }
}

package com.example.studentdashboard.controller;

import com.example.studentdashboard.dto.*;
import com.example.studentdashboard.entity.User;
import com.example.studentdashboard.repository.UserRepository;
import com.example.studentdashboard.service.MentorService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.util.List;

@RestController
@RequestMapping("/api/mentor")
@RequiredArgsConstructor
public class MentorController {

    private final MentorService mentorService;
    private final UserRepository userRepository;

    private Long getAuthenticatedMentorId(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Mentor not found"))
                .getId();
    }

    @GetMapping("/dashboard")
    public ResponseEntity<MentorDashboardDto> getDashboard(@AuthenticationPrincipal UserDetails userDetails) {
        Long mentorId = getAuthenticatedMentorId(userDetails);
        return ResponseEntity.ok(mentorService.getDashboard(mentorId));
    }

    @GetMapping("/students")
    public ResponseEntity<Page<MentorStudentSummaryDto>> getStudents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Long mentorId = getAuthenticatedMentorId(userDetails);
        return ResponseEntity.ok(mentorService.getStudents(mentorId, PageRequest.of(page, size)));
    }

    @GetMapping("/students/{studentId}")
    public ResponseEntity<StudentDetailsDto> getStudentDetails(
            @PathVariable Long studentId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Long mentorId = getAuthenticatedMentorId(userDetails);
        return ResponseEntity.ok(mentorService.getStudentDetails(mentorId, studentId));
    }

    @GetMapping("/students/{studentId}/progress")
    public ResponseEntity<List<LessonProgressDto>> getStudentProgress(
            @PathVariable Long studentId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Long mentorId = getAuthenticatedMentorId(userDetails);
        return ResponseEntity.ok(mentorService.getStudentProgress(mentorId, studentId));
    }

    @GetMapping("/students/{studentId}/activity")
    public ResponseEntity<List<ActivityEventDto>> getStudentActivity(
            @PathVariable Long studentId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Long mentorId = getAuthenticatedMentorId(userDetails);
        return ResponseEntity.ok(mentorService.getStudentActivity(mentorId, studentId));
    }

    @GetMapping("/students/{studentId}/analytics")
    public ResponseEntity<LearningTrendDto> getStudentAnalytics(
            @PathVariable Long studentId,
            @RequestParam(defaultValue = "7d") String range,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Long mentorId = getAuthenticatedMentorId(userDetails);
        return ResponseEntity.ok(mentorService.getStudentAnalytics(mentorId, studentId, range));
    }

    @GetMapping("/export/students.csv")
    public ResponseEntity<Resource> exportStudentsCsv(@AuthenticationPrincipal UserDetails userDetails) {
        Long mentorId = getAuthenticatedMentorId(userDetails);
        ByteArrayInputStream in = mentorService.exportStudentsCsv(mentorId);
        InputStreamResource file = new InputStreamResource(in);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=students.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(file);
    }
}

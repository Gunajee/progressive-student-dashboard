package com.example.studentdashboard.controller;

import com.example.studentdashboard.dto.ActivityEventDto;
import com.example.studentdashboard.repository.UserRepository;
import com.example.studentdashboard.service.ActivityService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/student/activity")
@RequiredArgsConstructor
public class StudentActivityController {

    private final ActivityService activityService;
    private final UserRepository userRepository;

    private Long getAuthenticatedStudentId(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("User not found"))
                .getId();
    }

    @GetMapping
    public ResponseEntity<Page<ActivityEventDto>> getActivity(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Long studentId = getAuthenticatedStudentId(userDetails);
        return ResponseEntity.ok(activityService.getStudentActivity(studentId, PageRequest.of(page, size)));
    }

    @GetMapping("/recent")
    public ResponseEntity<List<ActivityEventDto>> getRecentActivity(
            @RequestParam(defaultValue = "5") int limit,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Long studentId = getAuthenticatedStudentId(userDetails);
        return ResponseEntity.ok(activityService.getRecentActivity(studentId, limit));
    }
}

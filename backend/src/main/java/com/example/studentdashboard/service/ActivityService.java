package com.example.studentdashboard.service;

import com.example.studentdashboard.dto.ActivityEventDto;
import com.example.studentdashboard.entity.ActivityEventType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ActivityService {
    Page<ActivityEventDto> getStudentActivity(Long studentId, Pageable pageable);
    List<ActivityEventDto> getRecentActivity(Long studentId, int limit);
    void logActivity(Long studentId, Long courseId, Long lessonId, ActivityEventType eventType, int durationMinutes, String metadata);
    int calculateStreak(Long studentId);
}

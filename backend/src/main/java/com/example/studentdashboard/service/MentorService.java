package com.example.studentdashboard.service;

import com.example.studentdashboard.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.ByteArrayInputStream;
import java.util.List;

public interface MentorService {
    MentorDashboardDto getDashboard(Long mentorId);
    Page<MentorStudentSummaryDto> getStudents(Long mentorId, Pageable pageable);
    StudentDetailsDto getStudentDetails(Long mentorId, Long studentId);
    List<LessonProgressDto> getStudentProgress(Long mentorId, Long studentId);
    List<ActivityEventDto> getStudentActivity(Long mentorId, Long studentId);
    LearningTrendDto getStudentAnalytics(Long mentorId, Long studentId, String range);
    ByteArrayInputStream exportStudentsCsv(Long mentorId);
}

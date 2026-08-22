package com.example.studentdashboard.service;

import com.example.studentdashboard.dto.DashboardDto;

public interface StudentDashboardService {
    DashboardDto getDashboard(Long studentId);
}

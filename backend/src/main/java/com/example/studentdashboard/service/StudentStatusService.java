package com.example.studentdashboard.service;

import com.example.studentdashboard.dto.StudentStatus;

public interface StudentStatusService {
    StudentStatus calculateStatus(Long studentId);
}

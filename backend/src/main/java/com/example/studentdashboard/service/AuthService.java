package com.example.studentdashboard.service;

import com.example.studentdashboard.dto.AuthResponse;
import com.example.studentdashboard.dto.LoginRequest;
import com.example.studentdashboard.dto.RegisterRequest;
import com.example.studentdashboard.dto.UserDto;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    UserDto getCurrentUser(String email);
}

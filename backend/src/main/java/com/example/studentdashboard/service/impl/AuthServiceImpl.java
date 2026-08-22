package com.example.studentdashboard.service.impl;

import com.example.studentdashboard.dto.AuthResponse;
import com.example.studentdashboard.dto.LoginRequest;
import com.example.studentdashboard.dto.RegisterRequest;
import com.example.studentdashboard.dto.UserDto;
import com.example.studentdashboard.entity.Enrollment;
import com.example.studentdashboard.entity.EnrollmentStatus;
import com.example.studentdashboard.entity.Role;
import com.example.studentdashboard.entity.User;
import com.example.studentdashboard.repository.CourseRepository;
import com.example.studentdashboard.repository.EnrollmentRepository;
import com.example.studentdashboard.repository.UserRepository;
import com.example.studentdashboard.security.CustomUserDetailsService;
import com.example.studentdashboard.security.JwtService;
import com.example.studentdashboard.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;
    private final AuthenticationManager authenticationManager;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already exists");
        }

        User mentor = userRepository.findAll().stream()
                .filter(u -> u.getRole() == Role.MENTOR)
                .findFirst()
                .orElse(null);

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.STUDENT)
                .mentor(mentor)
                .build();

        user = userRepository.save(user);

        // Auto enroll in all baseline courses
        final User studentRef = user;
        courseRepository.findAll().forEach(course -> {
            Enrollment enrollment = Enrollment.builder()
                    .student(studentRef)
                    .course(course)
                    .enrolledAt(java.time.LocalDateTime.now())
                    .status(EnrollmentStatus.ACTIVE)
                    .build();
            enrollmentRepository.save(enrollment);
        });

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String token = jwtService.generateToken(userDetails);

        return AuthResponse.builder()
                .token(token)
                .user(mapToUserDto(user))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String token = jwtService.generateToken(userDetails);

        return AuthResponse.builder()
                .token(token)
                .user(mapToUserDto(user))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto getCurrentUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return mapToUserDto(user);
    }

    private UserDto mapToUserDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }
}

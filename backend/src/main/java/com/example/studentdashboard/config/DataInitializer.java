package com.example.studentdashboard.config;

import com.example.studentdashboard.entity.*;
import com.example.studentdashboard.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
@Slf4j
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final LessonRepository lessonRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() == 0) {
            log.info("Database is empty. Initializing baseline seed data...");

            // 1. Create mentor
            User mentor = User.builder()
                    .name("Default Mentor")
                    .email("mentor@example.com")
                    .password(passwordEncoder.encode("password"))
                    .role(Role.MENTOR)
                    .build();
            mentor = userRepository.save(mentor);
            log.info("Seeded mentor account: mentor@example.com / password");

            // 2. Create React Fundamentals course
            Course reactCourse = Course.builder()
                    .title("React Fundamentals")
                    .category("Frontend")
                    .difficulty(Difficulty.BEGINNER)
                    .estimatedHours(6)
                    .description("Master the fundamentals of React including components, props, state, hooks, and lifecycle.")
                    .build();
            reactCourse = courseRepository.save(reactCourse);

            // 3. Create lessons
            Lesson l1 = Lesson.builder().course(reactCourse).title("Introduction to React").orderIndex(1).estimatedMinutes(20).description("What is React, virtual DOM, and setting up Vite project.").build();
            Lesson l2 = Lesson.builder().course(reactCourse).title("Components & Props").orderIndex(2).estimatedMinutes(30).description("Functional components, jsx rendering, and props declaration.").build();
            Lesson l3 = Lesson.builder().course(reactCourse).title("State & Lifecycle").orderIndex(3).estimatedMinutes(40).description("useState hook, state management rules, and side effects.").build();
            
            lessonRepository.saveAll(Arrays.asList(l1, l2, l3));
            log.info("Seeded React Fundamentals course syllabus.");

        } else {
            log.info("Database already contains records. Skipping initialization.");
        }
    }
}

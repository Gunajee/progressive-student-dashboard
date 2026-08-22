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
        // 1. Ensure Seed Mentor exists
        if (userRepository.findByEmail("mentor@example.com").isEmpty()) {
            User mentor = User.builder()
                    .name("Default Mentor")
                    .email("mentor@example.com")
                    .password(passwordEncoder.encode("password"))
                    .role(Role.MENTOR)
                    .build();
            userRepository.save(mentor);
            log.info("Seeded mentor account: mentor@example.com / password");
        }

        // 2. Ensure Seed Admin exists
        if (userRepository.findByEmail("admin@example.com").isEmpty()) {
            User admin = User.builder()
                    .name("System Admin")
                    .email("admin@example.com")
                    .password(passwordEncoder.encode("password"))
                    .role(Role.ADMIN)
                    .build();
            userRepository.save(admin);
            log.info("Seeded admin account: admin@example.com / password");
        }

        // 3. Ensure Seed Courses exist
        if (courseRepository.count() == 0) {
            log.info("Database contains no courses. Initializing baseline course data...");

            Course reactCourse = Course.builder()
                    .title("React Fundamentals")
                    .category("Frontend")
                    .difficulty(Difficulty.BEGINNER)
                    .estimatedHours(6)
                    .description("Master the fundamentals of React including components, props, state, hooks, and lifecycle.")
                    .build();
            reactCourse = courseRepository.save(reactCourse);

            String l1Content = "# Welcome to React\nReact is a library for building user interfaces.\n\n## Core Concepts\n- Virtual DOM\n- JSX\n- Components\n\n```javascript\nfunction Hello() {\n  return <h1>Hello World</h1>;\n}\n```";
            String l2Content = "# Components & Props\nComponents let you split the UI into independent, reusable pieces.\n\n### Using Props\nProps are read-only.\n```javascript\nfunction Welcome(props) {\n  return <h1>Hello, {props.name}</h1>;\n}\n```";
            String l3Content = "# State & Lifecycle\nState allows React components to change their output over time in response to user actions.\n\n### The useState Hook\n```javascript\nconst [count, setCount] = useState(0);\n```";

            Lesson l1 = Lesson.builder().course(reactCourse).title("Introduction to React").orderIndex(1).estimatedMinutes(20).description("What is React, virtual DOM, and setting up Vite project.").content(l1Content).build();
            Lesson l2 = Lesson.builder().course(reactCourse).title("Components & Props").orderIndex(2).estimatedMinutes(30).description("Functional components, jsx rendering, and props declaration.").content(l2Content).build();
            Lesson l3 = Lesson.builder().course(reactCourse).title("State & Lifecycle").orderIndex(3).estimatedMinutes(40).description("useState hook, state management rules, and side effects.").content(l3Content).build();

            lessonRepository.saveAll(Arrays.asList(l1, l2, l3));
            log.info("Seeded React Fundamentals course syllabus.");
        }
    }
}

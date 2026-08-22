package com.example.studentdashboard;

import com.example.studentdashboard.dto.ProgressUpdateDto;
import com.example.studentdashboard.entity.*;
import com.example.studentdashboard.repository.*;
import com.example.studentdashboard.security.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class MentorApiTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private LessonRepository lessonRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private LessonProgressRepository lessonProgressRepository;

    @Autowired
    private ActivityEventRepository activityEventRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    private User mentorA;
    private User mentorB;
    private User studentA;
    private User studentB;

    private Course course;
    private Lesson lesson1;
    private Lesson lesson2;
    private Lesson lesson3;

    private String mentorTokenA;
    private String mentorTokenB;
    private String studentTokenA;

    @BeforeEach
    public void setup() {
        // Clear database
        activityEventRepository.deleteAll();
        lessonProgressRepository.deleteAll();
        enrollmentRepository.deleteAll();
        lessonRepository.deleteAll();
        courseRepository.deleteAll();
        userRepository.deleteAll();

        // 1. Create mentors
        mentorA = userRepository.save(User.builder()
                .name("Mentor Alice")
                .email("alice@mentor.com")
                .password(passwordEncoder.encode("password"))
                .role(Role.MENTOR)
                .build());

        mentorB = userRepository.save(User.builder()
                .name("Mentor Bob")
                .email("bob@mentor.com")
                .password(passwordEncoder.encode("password"))
                .role(Role.MENTOR)
                .build());

        // 2. Create students assigned to mentors
        studentA = userRepository.save(User.builder()
                .name("Student Alan")
                .email("alan@student.com")
                .password(passwordEncoder.encode("password"))
                .role(Role.STUDENT)
                .mentor(mentorA)
                .build());

        studentB = userRepository.save(User.builder()
                .name("Student Betty")
                .email("betty@student.com")
                .password(passwordEncoder.encode("password"))
                .role(Role.STUDENT)
                .mentor(mentorB)
                .build());

        // 3. Create course and 3 lessons
        course = courseRepository.save(Course.builder()
                .title("Advanced React")
                .category("Frontend")
                .difficulty(Difficulty.ADVANCED)
                .estimatedHours(10)
                .build());

        lesson1 = lessonRepository.save(Lesson.builder().course(course).title("Hooks").orderIndex(1).estimatedMinutes(30).build());
        lesson2 = lessonRepository.save(Lesson.builder().course(course).title("Suspense").orderIndex(2).estimatedMinutes(30).build());
        lesson3 = lessonRepository.save(Lesson.builder().course(course).title("Concurrent Mode").orderIndex(3).estimatedMinutes(30).build());

        // 4. Enroll Student A and Student B
        enrollmentRepository.save(Enrollment.builder().student(studentA).course(course).status(EnrollmentStatus.ACTIVE).build());
        enrollmentRepository.save(Enrollment.builder().student(studentB).course(course).status(EnrollmentStatus.ACTIVE).build());

        // 5. Generate tokens
        org.springframework.security.core.userdetails.User userAlice =
                new org.springframework.security.core.userdetails.User(mentorA.getEmail(), "",
                        Collections.singletonList(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_MENTOR")));
        mentorTokenA = jwtService.generateToken(userAlice);

        org.springframework.security.core.userdetails.User userBob =
                new org.springframework.security.core.userdetails.User(mentorB.getEmail(), "",
                        Collections.singletonList(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_MENTOR")));
        mentorTokenB = jwtService.generateToken(userBob);

        org.springframework.security.core.userdetails.User userAlan =
                new org.springframework.security.core.userdetails.User(studentA.getEmail(), "",
                        Collections.singletonList(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_STUDENT")));
        studentTokenA = jwtService.generateToken(userAlan);
    }

    @Test
    public void testMentorDashboardAndStudentListPagination() throws Exception {
        // 1. Log activity today for student A to avoid 5-day inactivity rule
        activityEventRepository.save(ActivityEvent.builder()
                .student(studentA)
                .eventType(ActivityEventType.STUDY_SESSION)
                .durationMinutes(30)
                .eventDate(LocalDateTime.now())
                .build());

        // 2. Fetch mentor A dashboard. Alan has 0% progress -> AT_RISK. Inactivity is not matching since we logged activity.
        mockMvc.perform(get("/api/mentor/dashboard")
                        .header("Authorization", "Bearer " + mentorTokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalStudents", is(1)))
                .andExpect(jsonPath("$.activeStudents", is(1)))
                .andExpect(jsonPath("$.averageProgress", is(0.0)))
                .andExpect(jsonPath("$.atRiskStudents", is(1)));

        // 3. Fetch students list with pagination
        mockMvc.perform(get("/api/mentor/students?page=0&size=5")
                        .header("Authorization", "Bearer " + mentorTokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].student.name", is("Student Alan")))
                .andExpect(jsonPath("$.content[0].status", is("AT_RISK")))
                .andExpect(jsonPath("$.content[0].courseCount", is(1)))
                .andExpect(jsonPath("$.totalPages", is(1)))
                .andExpect(jsonPath("$.totalElements", is(1)));
    }

    @Test
    public void testAtRiskDetectionRules() throws Exception {
        // Log study time today
        activityEventRepository.save(ActivityEvent.builder()
                .student(studentA)
                .eventType(ActivityEventType.STUDY_SESSION)
                .durationMinutes(30)
                .eventDate(LocalDateTime.now())
                .build());

        // A. Progress = 0% (< 40%) -> AT_RISK
        mockMvc.perform(get("/api/mentor/students/" + studentA.getId())
                        .header("Authorization", "Bearer " + mentorTokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("AT_RISK")));

        // B. Progress = 2 of 3 completed = 66.6% -> NEEDS_ATTENTION
        lessonProgressRepository.save(LessonProgress.builder().student(studentA).lesson(lesson1).status(LessonProgressStatus.COMPLETED).timeSpentMinutes(30).build());
        lessonProgressRepository.save(LessonProgress.builder().student(studentA).lesson(lesson2).status(LessonProgressStatus.COMPLETED).timeSpentMinutes(30).build());
        
        mockMvc.perform(get("/api/mentor/students/" + studentA.getId())
                        .header("Authorization", "Bearer " + mentorTokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("NEEDS_ATTENTION")));

        // C. Progress = 3 of 3 completed = 100% -> HEALTHY
        lessonProgressRepository.save(LessonProgress.builder().student(studentA).lesson(lesson3).status(LessonProgressStatus.COMPLETED).timeSpentMinutes(30).build());
        
        mockMvc.perform(get("/api/mentor/students/" + studentA.getId())
                        .header("Authorization", "Bearer " + mentorTokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("HEALTHY")));

        // D. Progress = 100% (normally HEALTHY) but inactivity >= 5 days -> AT_RISK
        activityEventRepository.deleteAll();
        activityEventRepository.save(ActivityEvent.builder()
                .student(studentA)
                .eventType(ActivityEventType.STUDY_SESSION)
                .durationMinutes(30)
                .eventDate(LocalDateTime.now().minusDays(6))
                .build());

        mockMvc.perform(get("/api/mentor/students/" + studentA.getId())
                        .header("Authorization", "Bearer " + mentorTokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("AT_RISK")));
    }

    @Test
    public void testMentorSecurityAccessControls() throws Exception {
        // A. Student trying to access mentor APIs -> 403 Forbidden
        mockMvc.perform(get("/api/mentor/dashboard")
                        .header("Authorization", "Bearer " + studentTokenA))
                .andExpect(status().isForbidden());

        // B. Mentor A trying to access student B (assigned to Mentor B) -> 403 Forbidden
        mockMvc.perform(get("/api/mentor/students/" + studentB.getId())
                        .header("Authorization", "Bearer " + mentorTokenA))
                .andExpect(status().isForbidden());

        // C. Mentor A trying to access student A -> 200 OK
        mockMvc.perform(get("/api/mentor/students/" + studentA.getId())
                        .header("Authorization", "Bearer " + mentorTokenA))
                .andExpect(status().isOk());
    }

    @Test
    public void testCsvExport() throws Exception {
        // Export CSV for mentor A
        mockMvc.perform(get("/api/mentor/export/students.csv")
                        .header("Authorization", "Bearer " + mentorTokenA))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=students.csv"))
                .andExpect(content().contentType("text/csv"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Student,Email,Courses,Completed Lessons,Overall Progress,Learning Hours,Last Active,Status")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Student Alan")));
    }
}

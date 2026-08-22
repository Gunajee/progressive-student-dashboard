package com.example.studentdashboard;

import com.example.studentdashboard.dto.LoginRequest;
import com.example.studentdashboard.dto.ProgressUpdateDto;
import com.example.studentdashboard.entity.*;
import com.example.studentdashboard.repository.*;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class StudentApiTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private com.example.studentdashboard.service.ActivityService activityService;

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
    private ObjectMapper objectMapper;

    private String studentToken;
    private User student;
    private Course course;
    private Lesson lesson1;
    private Lesson lesson2;

    @BeforeEach
    public void setup() throws Exception {
        // Clear all
        activityEventRepository.deleteAll();
        lessonProgressRepository.deleteAll();
        enrollmentRepository.deleteAll();
        lessonRepository.deleteAll();
        courseRepository.deleteAll();
        userRepository.deleteAll();

        // 1. Create student
        student = User.builder()
                .name("Alice Student")
                .email("alice@test.com")
                .password(passwordEncoder.encode("password"))
                .role(Role.STUDENT)
                .build();
        student = userRepository.save(student);

        // 2. Create course and lessons
        course = courseRepository.save(Course.builder()
                .title("React Basics")
                .category("Frontend")
                .difficulty(Difficulty.BEGINNER)
                .estimatedHours(10)
                .build());

        lesson1 = lessonRepository.save(Lesson.builder()
                .course(course)
                .title("Components")
                .orderIndex(1)
                .estimatedMinutes(20)
                .build());

        lesson2 = lessonRepository.save(Lesson.builder()
                .course(course)
                .title("Props & State")
                .orderIndex(2)
                .estimatedMinutes(30)
                .build());

        // 3. Enroll student in course
        enrollmentRepository.save(Enrollment.builder()
                .student(student)
                .course(course)
                .status(EnrollmentStatus.ACTIVE)
                .build());

        // 4. Authenticate student to fetch JWT token
        String loginResponse = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest("alice@test.com", "password"))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        studentToken = objectMapper.readTree(loginResponse).get("token").asText();
    }

    @Test
    public void testStudentDashboardAndAPIExecution() throws Exception {
        // 1. Check enrolled courses retrieval
        mockMvc.perform(get("/api/student/courses")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title", is("React Basics")))
                .andExpect(jsonPath("$[0].progressPercentage", is(0.0)));

        // 2. Check course syllabus retrieval
        mockMvc.perform(get("/api/student/courses/" + course.getId() + "/lessons")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].title", is("Components")))
                .andExpect(jsonPath("$[0].status", is("NOT_STARTED")));

        // 3. Update lesson progress to IN_PROGRESS
        mockMvc.perform(put("/api/student/lessons/" + lesson1.getId() + "/progress")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ProgressUpdateDto(LessonProgressStatus.IN_PROGRESS, 10))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("IN_PROGRESS")))
                .andExpect(jsonPath("$.timeSpentMinutes", is(10)));

        // 4. Check dashboard calculations
        mockMvc.perform(get("/api/student/dashboard")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCourses", is(1)))
                .andExpect(jsonPath("$.completedLessons", is(0)))
                .andExpect(jsonPath("$.totalLearningMinutes", is(10)))
                .andExpect(jsonPath("$.overallProgress", is(0.0)))
                .andExpect(jsonPath("$.courseProgress[0].progressPercentage", is(0.0)))
                .andExpect(jsonPath("$.recommendations[0].type", is("RESUME_LESSON")))
                .andExpect(jsonPath("$.recommendations[0].title", is("Complete Lesson: Components")));

        // 5. Update lesson progress to COMPLETED
        mockMvc.perform(put("/api/student/lessons/" + lesson1.getId() + "/progress")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ProgressUpdateDto(LessonProgressStatus.COMPLETED, 15))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("COMPLETED")))
                .andExpect(jsonPath("$.timeSpentMinutes", is(25)));

        // 6. Check updated dashboard progress percentage calculations
        mockMvc.perform(get("/api/student/dashboard")
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completedLessons", is(1)))
                .andExpect(jsonPath("$.totalLearningMinutes", is(25)))
                .andExpect(jsonPath("$.overallProgress", is(50.0))) // 1 of 2 lessons complete = 50%
                .andExpect(jsonPath("$.courseProgress[0].progressPercentage", is(50.0)))
                .andExpect(jsonPath("$.recommendations[0].type", is("NEXT_LESSON")))
                .andExpect(jsonPath("$.recommendations[0].title", is("Next Lesson: Props & State")));
    }

    @Test
    public void testLearningStreakCalculation() {
        // Clear events
        activityEventRepository.deleteAll();

        // 1. Empty events = 0 streak
        int streak = activityEventRepository.findByStudentIdOrderByEventDateDesc(student.getId()).size();
        assertThat(streak).isEqualTo(0);

        // 2. Add events spanning consecutive days
        LocalDateTime today = LocalDateTime.now();
        LocalDateTime yesterday = today.minusDays(1);
        LocalDateTime twoDaysAgo = today.minusDays(2);
        LocalDateTime fourDaysAgo = today.minusDays(4); // Gap day

        activityEventRepository.save(ActivityEvent.builder().student(student).eventType(ActivityEventType.STUDY_SESSION).durationMinutes(10).eventDate(today).build());
        activityEventRepository.save(ActivityEvent.builder().student(student).eventType(ActivityEventType.STUDY_SESSION).durationMinutes(15).eventDate(yesterday).build());
        activityEventRepository.save(ActivityEvent.builder().student(student).eventType(ActivityEventType.STUDY_SESSION).durationMinutes(20).eventDate(twoDaysAgo).build());
        activityEventRepository.save(ActivityEvent.builder().student(student).eventType(ActivityEventType.STUDY_SESSION).durationMinutes(30).eventDate(fourDaysAgo).build());

        // Validate streak calculation programmatically
        // We will call the calculated streak method from ActivityService
        // Alice should have a 3-day consecutive streak because of the gap on day 3
        int calculatedStreak = activityService.calculateStreak(student.getId());

        assertThat(calculatedStreak).isEqualTo(3);
    }
}

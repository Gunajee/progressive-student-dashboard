package com.example.studentdashboard;

import com.example.studentdashboard.dto.RecommendationDto;
import com.example.studentdashboard.entity.*;
import com.example.studentdashboard.repository.*;
import com.example.studentdashboard.service.RecommendationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
public class AnalyticsAndRecommendationTests {

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
    private RecommendationService recommendationService;

    private User student;
    private Course course;
    private Lesson lesson1;
    private Lesson lesson2;
    private Lesson lesson3;
    private Lesson lesson4;
    private Lesson lesson5;

    @BeforeEach
    public void setup() {
        // Clear database
        activityEventRepository.deleteAll();
        lessonProgressRepository.deleteAll();
        enrollmentRepository.deleteAll();
        lessonRepository.deleteAll();
        courseRepository.deleteAll();
        userRepository.deleteAll();

        // 1. Create student
        student = userRepository.save(User.builder()
                .name("Test Student")
                .email("test.student@test.com")
                .password("password")
                .role(Role.STUDENT)
                .build());

        // 2. Create course
        course = courseRepository.save(Course.builder()
                .title("Spring Boot Essentials")
                .category("Backend")
                .difficulty(Difficulty.INTERMEDIATE)
                .estimatedHours(15)
                .build());

        // 3. Create 5 lessons
        lesson1 = lessonRepository.save(Lesson.builder().course(course).title("Lesson 1").orderIndex(1).estimatedMinutes(30).build());
        lesson2 = lessonRepository.save(Lesson.builder().course(course).title("Lesson 2").orderIndex(2).estimatedMinutes(30).build());
        lesson3 = lessonRepository.save(Lesson.builder().course(course).title("Lesson 3").orderIndex(3).estimatedMinutes(30).build());
        lesson4 = lessonRepository.save(Lesson.builder().course(course).title("Lesson 4").orderIndex(4).estimatedMinutes(30).build());
        lesson5 = lessonRepository.save(Lesson.builder().course(course).title("Lesson 5").orderIndex(5).estimatedMinutes(30).build());
    }

    @Test
    public void testNoEnrollmentsEdgeCase() {
        List<RecommendationDto> recommendations = recommendationService.getRecommendations(student.getId());
        assertThat(recommendations).isEmpty();
    }

    @Test
    public void testInactivityRuleAndLowProgressRule() {
        // Enroll student in course
        enrollmentRepository.save(Enrollment.builder()
                .student(student)
                .course(course)
                .status(EnrollmentStatus.ACTIVE)
                .build());

        // Set one lesson to IN_PROGRESS (so progress is 0%, meaning low progress isn't evaluated, but next lesson is)
        lessonProgressRepository.save(LessonProgress.builder()
                .student(student)
                .lesson(lesson1)
                .status(LessonProgressStatus.IN_PROGRESS)
                .timeSpentMinutes(10)
                .build());

        // Save a single activity event 5 days ago to trigger inactivity
        activityEventRepository.save(ActivityEvent.builder()
                .student(student)
                .eventType(ActivityEventType.STUDY_SESSION)
                .durationMinutes(10)
                .eventDate(LocalDateTime.now().minusDays(5))
                .build());

        List<RecommendationDto> recommendations = recommendationService.getRecommendations(student.getId());

        // Should have INACTIVITY (HIGH), UNFINISHED_LESSON (HIGH), and NEXT_LESSON (MEDIUM)
        assertThat(recommendations).isNotEmpty();
        
        Optional<RecommendationDto> inactivityRec = recommendations.stream()
                .filter(r -> "INACTIVITY".equals(r.getType()))
                .findFirst();
        assertThat(inactivityRec).isPresent();
        assertThat(inactivityRec.get().getPriority()).isEqualTo("HIGH");
        assertThat(inactivityRec.get().getReason()).contains("3 days");

        Optional<RecommendationDto> unfinishedRec = recommendations.stream()
                .filter(r -> "RESUME_LESSON".equals(r.getType()))
                .findFirst();
        assertThat(unfinishedRec).isPresent();
        assertThat(unfinishedRec.get().getPriority()).isEqualTo("HIGH");
        assertThat(unfinishedRec.get().getLessonId()).isEqualTo(lesson1.getId());
    }

    @Test
    public void testAlmostCompleteAndLowProgressRules() {
        // Enroll student
        enrollmentRepository.save(Enrollment.builder().student(student).course(course).status(EnrollmentStatus.ACTIVE).build());
        
        // Log activity today to avoid inactivity rule
        activityEventRepository.save(ActivityEvent.builder().student(student).eventType(ActivityEventType.STUDY_SESSION).durationMinutes(10).eventDate(LocalDateTime.now()).build());

        // Case A: 1 of 5 completed = 20% progress -> Low Progress Rule (LOW) should fire
        lessonProgressRepository.save(LessonProgress.builder()
                .student(student)
                .lesson(lesson1)
                .status(LessonProgressStatus.COMPLETED)
                .completedAt(LocalDateTime.now())
                .timeSpentMinutes(30)
                .build());

        List<RecommendationDto> recommendationsA = recommendationService.getRecommendations(student.getId());
        
        Optional<RecommendationDto> lowProgressRec = recommendationsA.stream()
                .filter(r -> "LOW_PROGRESS".equals(r.getType()))
                .findFirst();
        assertThat(lowProgressRec).isPresent();
        assertThat(lowProgressRec.get().getPriority()).isEqualTo("LOW");
        assertThat(lowProgressRec.get().getReason()).contains("20.0%");

        // Case B: 4 of 5 completed = 80% progress -> Almost Complete Rule (MEDIUM) should fire
        lessonProgressRepository.deleteAll();
        lessonProgressRepository.flush();
        lessonProgressRepository.save(LessonProgress.builder().student(student).lesson(lesson1).status(LessonProgressStatus.COMPLETED).timeSpentMinutes(30).build());
        lessonProgressRepository.save(LessonProgress.builder().student(student).lesson(lesson2).status(LessonProgressStatus.COMPLETED).timeSpentMinutes(30).build());
        lessonProgressRepository.save(LessonProgress.builder().student(student).lesson(lesson3).status(LessonProgressStatus.COMPLETED).timeSpentMinutes(30).build());
        lessonProgressRepository.save(LessonProgress.builder().student(student).lesson(lesson4).status(LessonProgressStatus.COMPLETED).timeSpentMinutes(30).build());

        List<RecommendationDto> recommendationsB = recommendationService.getRecommendations(student.getId());
        
        Optional<RecommendationDto> almostCompleteRec = recommendationsB.stream()
                .filter(r -> "ALMOST_COMPLETE".equals(r.getType()))
                .findFirst();
        assertThat(almostCompleteRec).isPresent();
        assertThat(almostCompleteRec.get().getPriority()).isEqualTo("MEDIUM");
        assertThat(almostCompleteRec.get().getReason()).contains("80.0%");

        // Verify Rule 1 Next Lesson points to Lesson 5
        Optional<RecommendationDto> nextLessonRec = recommendationsB.stream()
                .filter(r -> "NEXT_LESSON".equals(r.getType()))
                .findFirst();
        assertThat(nextLessonRec).isPresent();
        assertThat(nextLessonRec.get().getLessonId()).isEqualTo(lesson5.getId());
    }

    @Test
    public void testAllCoursesCompletedEdgeCase() {
        enrollmentRepository.save(Enrollment.builder().student(student).course(course).status(EnrollmentStatus.ACTIVE).build());
        activityEventRepository.save(ActivityEvent.builder().student(student).eventType(ActivityEventType.STUDY_SESSION).durationMinutes(10).eventDate(LocalDateTime.now()).build());

        // Mark all 5 lessons completed
        lessonProgressRepository.save(LessonProgress.builder().student(student).lesson(lesson1).status(LessonProgressStatus.COMPLETED).timeSpentMinutes(30).build());
        lessonProgressRepository.save(LessonProgress.builder().student(student).lesson(lesson2).status(LessonProgressStatus.COMPLETED).timeSpentMinutes(30).build());
        lessonProgressRepository.save(LessonProgress.builder().student(student).lesson(lesson3).status(LessonProgressStatus.COMPLETED).timeSpentMinutes(30).build());
        lessonProgressRepository.save(LessonProgress.builder().student(student).lesson(lesson4).status(LessonProgressStatus.COMPLETED).timeSpentMinutes(30).build());
        lessonProgressRepository.save(LessonProgress.builder().student(student).lesson(lesson5).status(LessonProgressStatus.COMPLETED).timeSpentMinutes(30).build());

        List<RecommendationDto> recommendations = recommendationService.getRecommendations(student.getId());

        // No NEXT_LESSON, ALMOST_COMPLETE, or LOW_PROGRESS recommendations should appear since course progress is 100%
        long invalidRecommendations = recommendations.stream()
                .filter(r -> "NEXT_LESSON".equals(r.getType()) || "ALMOST_COMPLETE".equals(r.getType()) || "LOW_PROGRESS".equals(r.getType()))
                .count();
        assertThat(invalidRecommendations).isEqualTo(0);
    }
}

package com.example.studentdashboard;

import com.example.studentdashboard.entity.*;
import com.example.studentdashboard.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
public class PersistenceTests {

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

    @Test
    public void testCreateUserAndMentorRelationship() {
        User mentor = User.builder()
                .name("Mentor Bob")
                .email("bob@mentor.com")
                .password("password")
                .role(Role.MENTOR)
                .build();
        mentor = userRepository.save(mentor);

        User student = User.builder()
                .name("Student Alice")
                .email("alice@student.com")
                .password("password")
                .role(Role.STUDENT)
                .mentor(mentor)
                .build();
        student = userRepository.save(student);

        Optional<User> foundStudent = userRepository.findById(student.getId());
        assertThat(foundStudent).isPresent();
        assertThat(foundStudent.get().getMentor()).isNotNull();
        assertThat(foundStudent.get().getMentor().getName()).isEqualTo("Mentor Bob");
    }

    @Test
    public void testUserEmailUniqueness() {
        User user1 = User.builder()
                .name("User 1")
                .email("duplicate@test.com")
                .password("password")
                .role(Role.STUDENT)
                .build();
        userRepository.save(user1);

        User user2 = User.builder()
                .name("User 2")
                .email("duplicate@test.com")
                .password("password")
                .role(Role.STUDENT)
                .build();

        assertThatThrownBy(() -> userRepository.saveAndFlush(user2))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    public void testEnrollmentUniqueConstraint() {
        User student = userRepository.save(User.builder()
                .name("Student")
                .email("student@test.com")
                .password("password")
                .role(Role.STUDENT)
                .build());

        Course course = courseRepository.save(Course.builder()
                .title("React Basics")
                .category("Frontend")
                .difficulty(Difficulty.BEGINNER)
                .estimatedHours(10)
                .build());

        Enrollment enrollment1 = Enrollment.builder()
                .student(student)
                .course(course)
                .status(EnrollmentStatus.ACTIVE)
                .build();
        enrollmentRepository.save(enrollment1);

        Enrollment enrollment2 = Enrollment.builder()
                .student(student)
                .course(course)
                .status(EnrollmentStatus.ACTIVE)
                .build();

        assertThatThrownBy(() -> enrollmentRepository.saveAndFlush(enrollment2))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    public void testLessonProgressUniqueConstraint() {
        User student = userRepository.save(User.builder()
                .name("Student")
                .email("student2@test.com")
                .password("password")
                .role(Role.STUDENT)
                .build());

        Course course = courseRepository.save(Course.builder()
                .title("Spring Boot Basics")
                .category("Backend")
                .difficulty(Difficulty.INTERMEDIATE)
                .estimatedHours(15)
                .build());

        Lesson lesson = lessonRepository.save(Lesson.builder()
                .course(course)
                .title("Intro to JPA")
                .orderIndex(1)
                .estimatedMinutes(30)
                .build());

        LessonProgress progress1 = LessonProgress.builder()
                .student(student)
                .lesson(lesson)
                .status(LessonProgressStatus.IN_PROGRESS)
                .timeSpentMinutes(15)
                .build();
        lessonProgressRepository.save(progress1);

        LessonProgress progress2 = LessonProgress.builder()
                .student(student)
                .lesson(lesson)
                .status(LessonProgressStatus.COMPLETED)
                .timeSpentMinutes(20)
                .build();

        assertThatThrownBy(() -> lessonProgressRepository.saveAndFlush(progress2))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    public void testRepositoryLookups() {
        User student = userRepository.save(User.builder()
                .name("Alice Smith")
                .email("alice.smith@test.com")
                .password("password")
                .role(Role.STUDENT)
                .build());

        Course course = courseRepository.save(Course.builder()
                .title("MySQL Database Design")
                .category("Database")
                .difficulty(Difficulty.ADVANCED)
                .estimatedHours(8)
                .build());

        Lesson lesson = lessonRepository.save(Lesson.builder()
                .course(course)
                .title("Normalisation")
                .orderIndex(1)
                .estimatedMinutes(45)
                .build());

        // Save Enrollment
        enrollmentRepository.save(Enrollment.builder()
                .student(student)
                .course(course)
                .status(EnrollmentStatus.ACTIVE)
                .build());

        // Save Progress
        lessonProgressRepository.save(LessonProgress.builder()
                .student(student)
                .lesson(lesson)
                .status(LessonProgressStatus.COMPLETED)
                .completedAt(LocalDateTime.now())
                .timeSpentMinutes(40)
                .build());

        // Save Activity
        activityEventRepository.save(ActivityEvent.builder()
                .student(student)
                .course(course)
                .lesson(lesson)
                .eventType(ActivityEventType.STUDY_SESSION)
                .durationMinutes(40)
                .metadata("Study session on normalisation")
                .build());

        // Lookups validation
        List<Enrollment> enrollments = enrollmentRepository.findByStudentId(student.getId());
        assertThat(enrollments).hasSize(1);
        assertThat(enrollments.get(0).getCourse().getTitle()).isEqualTo("MySQL Database Design");

        Optional<LessonProgress> progress = lessonProgressRepository.findByStudentIdAndLessonId(student.getId(), lesson.getId());
        assertThat(progress).isPresent();
        assertThat(progress.get().getStatus()).isEqualTo(LessonProgressStatus.COMPLETED);

        List<ActivityEvent> activities = activityEventRepository.findByStudentIdOrderByEventDateDesc(student.getId());
        assertThat(activities).hasSize(1);
        assertThat(activities.get(0).getDurationMinutes()).isEqualTo(40);
    }
}

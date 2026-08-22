package com.example.studentdashboard.service.impl;

import com.example.studentdashboard.dto.*;
import com.example.studentdashboard.entity.*;
import com.example.studentdashboard.repository.*;
import com.example.studentdashboard.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MentorServiceImpl implements MentorService {

    private final UserRepository userRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final LessonProgressRepository lessonProgressRepository;
    private final ActivityEventRepository activityEventRepository;
    private final CourseService courseService;
    private final ActivityService activityService;
    private final AnalyticsService analyticsService;
    private final StudentStatusService studentStatusService;

    private User verifyStudentAssignedToMentor(Long mentorId, Long studentId) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found"));

        if (student.getMentor() == null || !student.getMentor().getId().equals(mentorId)) {
            throw new org.springframework.security.access.AccessDeniedException("Access Denied: Student not assigned to this mentor");
        }
        return student;
    }

    @Override
    @Transactional(readOnly = true)
    public MentorDashboardDto getDashboard(Long mentorId) {
        List<User> students = userRepository.findByMentorIdAndRole(mentorId, Role.STUDENT);
        int totalStudents = students.size();
        if (totalStudents == 0) {
            return MentorDashboardDto.builder()
                    .totalStudents(0)
                    .activeStudents(0)
                    .averageProgress(0.0)
                    .atRiskStudents(0)
                    .build();
        }

        int activeStudents = 0;
        double sumProgress = 0.0;
        int atRiskStudents = 0;

        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);

        for (User student : students) {
            // Calculate progress
            List<CourseDto> enrolledCourses = courseService.getEnrolledCourses(student.getId());
            double averageProgress = 0.0;
            if (!enrolledCourses.isEmpty()) {
                averageProgress = enrolledCourses.stream()
                        .mapToDouble(CourseDto::getProgressPercentage)
                        .average()
                        .orElse(0.0);
            }
            sumProgress += averageProgress;

            // Check if active in last 7 days
            List<ActivityEvent> events = activityEventRepository.findByStudentIdOrderByEventDateDesc(student.getId());
            if (!events.isEmpty()) {
                LocalDateTime lastEventDate = events.get(0).getEventDate();
                if (lastEventDate.isAfter(sevenDaysAgo)) {
                    activeStudents++;
                }
            }

            // Check status
            StudentStatus status = studentStatusService.calculateStatus(student.getId());
            if (status == StudentStatus.AT_RISK) {
                atRiskStudents++;
            }
        }

        double averageProgress = Math.round((sumProgress / totalStudents) * 10.0) / 10.0;

        return MentorDashboardDto.builder()
                .totalStudents(totalStudents)
                .activeStudents(activeStudents)
                .averageProgress(averageProgress)
                .atRiskStudents(atRiskStudents)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MentorStudentSummaryDto> getStudents(Long mentorId, Pageable pageable) {
        Page<User> studentsPage = userRepository.findByMentorIdAndRole(mentorId, Role.STUDENT, pageable);

        return studentsPage.map(student -> {
            List<Enrollment> enrollments = enrollmentRepository.findByStudentId(student.getId());
            int courseCount = enrollments.size();

            // overall progress
            List<CourseDto> enrolledCourses = courseService.getEnrolledCourses(student.getId());
            double averageProgress = 0.0;
            if (!enrolledCourses.isEmpty()) {
                averageProgress = enrolledCourses.stream()
                        .mapToDouble(CourseDto::getProgressPercentage)
                        .average()
                        .orElse(0.0);
            }
            averageProgress = Math.round(averageProgress * 10.0) / 10.0;

            // minutes
            List<ActivityEvent> events = activityEventRepository.findByStudentIdOrderByEventDateDesc(student.getId());
            int learningMinutes = events.stream()
                    .mapToInt(ActivityEvent::getDurationMinutes)
                    .sum();

            LocalDateTime lastActive = events.isEmpty() ? null : events.get(0).getEventDate();
            StudentStatus status = studentStatusService.calculateStatus(student.getId());

            UserDto studentDto = UserDto.builder()
                    .id(student.getId())
                    .name(student.getName())
                    .email(student.getEmail())
                    .role(student.getRole())
                    .build();

            return MentorStudentSummaryDto.builder()
                    .student(studentDto)
                    .courseCount(courseCount)
                    .overallProgress(averageProgress)
                    .learningMinutes(learningMinutes)
                    .lastActive(lastActive)
                    .status(status)
                    .build();
        });
    }

    @Override
    @Transactional(readOnly = true)
    public StudentDetailsDto getStudentDetails(Long mentorId, Long studentId) {
        User student = verifyStudentAssignedToMentor(mentorId, studentId);

        List<CourseDto> enrolledCourses = courseService.getEnrolledCourses(studentId);
        double averageProgress = 0.0;
        if (!enrolledCourses.isEmpty()) {
            averageProgress = enrolledCourses.stream()
                    .mapToDouble(CourseDto::getProgressPercentage)
                    .average()
                    .orElse(0.0);
        }
        averageProgress = Math.round(averageProgress * 10.0) / 10.0;

        List<ActivityEvent> events = activityEventRepository.findByStudentIdOrderByEventDateDesc(studentId);
        int learningMinutes = events.stream()
                .mapToInt(ActivityEvent::getDurationMinutes)
                .sum();

        StudentStatus status = studentStatusService.calculateStatus(studentId);
        int streak = activityService.calculateStreak(studentId);

        UserDto studentDto = UserDto.builder()
                .id(student.getId())
                .name(student.getName())
                .email(student.getEmail())
                .role(student.getRole())
                .build();

        return StudentDetailsDto.builder()
                .student(studentDto)
                .courseCount(enrolledCourses.size())
                .overallProgress(averageProgress)
                .learningMinutes(learningMinutes)
                .status(status)
                .currentStreak(streak)
                .courses(enrolledCourses)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<LessonProgressDto> getStudentProgress(Long mentorId, Long studentId) {
        verifyStudentAssignedToMentor(mentorId, studentId);

        List<LessonProgress> progressList = lessonProgressRepository.findByStudentId(studentId);
        return progressList.stream().map(p -> LessonProgressDto.builder()
                .id(p.getId())
                .lessonId(p.getLesson().getId())
                .lessonTitle(p.getLesson().getTitle())
                .status(p.getStatus())
                .timeSpentMinutes(p.getTimeSpentMinutes())
                .completedAt(p.getCompletedAt())
                .build()
        ).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ActivityEventDto> getStudentActivity(Long mentorId, Long studentId) {
        verifyStudentAssignedToMentor(mentorId, studentId);
        return activityService.getRecentActivity(studentId, 50);
    }

    @Override
    @Transactional(readOnly = true)
    public LearningTrendDto getStudentAnalytics(Long mentorId, Long studentId, String range) {
        verifyStudentAssignedToMentor(mentorId, studentId);
        return analyticsService.getLearningTrend(studentId, range);
    }

    @Override
    @Transactional(readOnly = true)
    public ByteArrayInputStream exportStudentsCsv(Long mentorId) {
        List<User> students = userRepository.findByMentorIdAndRole(mentorId, Role.STUDENT);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (PrintWriter writer = new PrintWriter(out)) {
            // Write Header
            writer.println("Student,Email,Courses,Completed Lessons,Overall Progress,Learning Hours,Last Active,Status");

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            for (User student : students) {
                // Enrolled courses
                List<CourseDto> enrolledCourses = courseService.getEnrolledCourses(student.getId());
                double averageProgress = 0.0;
                if (!enrolledCourses.isEmpty()) {
                    averageProgress = enrolledCourses.stream()
                            .mapToDouble(CourseDto::getProgressPercentage)
                            .average()
                            .orElse(0.0);
                }
                averageProgress = Math.round(averageProgress * 10.0) / 10.0;

                String coursesString = enrolledCourses.stream()
                        .map(CourseDto::getTitle)
                        .collect(Collectors.joining("; "));

                // Completed lessons
                long completedLessons = lessonProgressRepository.findByStudentId(student.getId()).stream()
                        .filter(p -> p.getStatus() == LessonProgressStatus.COMPLETED)
                        .count();

                // Learning hours
                List<ActivityEvent> events = activityEventRepository.findByStudentIdOrderByEventDateDesc(student.getId());
                int learningMinutes = events.stream()
                        .mapToInt(ActivityEvent::getDurationMinutes)
                        .sum();
                double learningHours = Math.round((learningMinutes / 60.0) * 10.0) / 10.0;

                // Last active
                String lastActiveStr = "N/A";
                if (!events.isEmpty()) {
                    lastActiveStr = events.get(0).getEventDate().format(formatter);
                }

                // Status
                StudentStatus status = studentStatusService.calculateStatus(student.getId());

                // Escape name and courses with double quotes to comply with standard RFC 4180
                String nameEscaped = "\"" + student.getName().replace("\"", "\"\"") + "\"";
                String coursesEscaped = "\"" + coursesString.replace("\"", "\"\"") + "\"";

                writer.println(String.format("%s,%s,%s,%d,%.1f%%,%.1fh,%s,%s",
                        nameEscaped,
                        student.getEmail(),
                        coursesEscaped,
                        completedLessons,
                        averageProgress,
                        learningHours,
                        lastActiveStr,
                        status.name()
                ));
            }
            writer.flush();
        }

        return new ByteArrayInputStream(out.toByteArray());
    }
}

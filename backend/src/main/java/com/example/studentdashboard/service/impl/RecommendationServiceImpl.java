package com.example.studentdashboard.service.impl;

import com.example.studentdashboard.dto.CourseDto;
import com.example.studentdashboard.dto.RecommendationDto;
import com.example.studentdashboard.entity.*;
import com.example.studentdashboard.repository.*;
import com.example.studentdashboard.service.CourseService;
import com.example.studentdashboard.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecommendationServiceImpl implements RecommendationService {

    private final EnrollmentRepository enrollmentRepository;
    private final LessonRepository lessonRepository;
    private final LessonProgressRepository lessonProgressRepository;
    private final ActivityEventRepository activityEventRepository;
    private final CourseService courseService;

    @Override
    @Transactional(readOnly = true)
    public List<RecommendationDto> getRecommendations(Long studentId) {
        List<RecommendationDto> list = new ArrayList<>();
        List<Enrollment> enrollments = enrollmentRepository.findByStudentId(studentId);
        if (enrollments.isEmpty()) {
            return list;
        }

        // Rule 3 - Inactivity (HIGH)
        List<ActivityEvent> events = activityEventRepository.findByStudentIdOrderByEventDateDesc(studentId);
        boolean isInactive = false;
        if (events.isEmpty()) {
            isInactive = true;
        } else {
            LocalDateTime lastEventDate = events.get(0).getEventDate();
            long daysBetween = ChronoUnit.DAYS.between(lastEventDate, LocalDateTime.now());
            if (daysBetween >= 3) {
                isInactive = true;
            }
        }

        if (isInactive) {
            list.add(RecommendationDto.builder()
                    .id("rule3_inactive_" + studentId)
                    .type("INACTIVITY")
                    .title("Resume Your Daily Learning Path")
                    .description("Consistency builds long-term memory.")
                    .priority("HIGH")
                    .reason("You haven't recorded any study sessions in the last 3 days. Return to learning to maintain your streak.")
                    .build());
        }

        // Rule 2 - Unfinished lesson (HIGH)
        List<LessonProgress> inProgressList = lessonProgressRepository.findByStudentId(studentId).stream()
                .filter(p -> p.getStatus() == LessonProgressStatus.IN_PROGRESS)
                .collect(Collectors.toList());

        for (LessonProgress progress : inProgressList) {
            list.add(RecommendationDto.builder()
                    .id("rule2_unfinished_" + progress.getLesson().getId())
                    .type("RESUME_LESSON")
                    .title("Complete Lesson: " + progress.getLesson().getTitle())
                    .description("Finish what you started.")
                    .priority("HIGH")
                    .courseId(progress.getLesson().getCourse().getId())
                    .lessonId(progress.getLesson().getId())
                    .reason("You spent " + progress.getTimeSpentMinutes() + " minutes on " + progress.getLesson().getTitle() + " in " + progress.getLesson().getCourse().getTitle() + ". Complete it to wrap up the topic.")
                    .build());
        }

        // Evaluate course-specific rules
        for (Enrollment enrollment : enrollments) {
            Course course = enrollment.getCourse();
            double progress = courseService.calculateCourseProgress(studentId, course.getId());
            List<Lesson> lessons = lessonRepository.findByCourseIdOrderByOrderIndexAsc(course.getId());
            List<LessonProgress> progresses = lessonProgressRepository.findByStudentIdAndLessonCourseId(studentId, course.getId());

            long completedCount = progresses.stream()
                    .filter(p -> p.getStatus() == LessonProgressStatus.COMPLETED)
                    .count();

            // Rule 4 - Almost complete (MEDIUM)
            if (progress >= 80.0 && progress < 100.0) {
                list.add(RecommendationDto.builder()
                        .id("rule4_almost_complete_" + course.getId())
                        .type("ALMOST_COMPLETE")
                        .title("Finish Course: " + course.getTitle())
                        .description("You are close to the finish line!")
                        .priority("MEDIUM")
                        .courseId(course.getId())
                        .reason("Your progress in " + course.getTitle() + " is " + progress + "%. Complete the remaining lessons to finish the course.")
                        .build());
            }

            // Rule 5 - Low progress (LOW)
            if (progress < 40.0 && progress > 0.0) {
                list.add(RecommendationDto.builder()
                        .id("rule5_low_progress_" + course.getId())
                        .type("LOW_PROGRESS")
                        .title("Build Momentum in: " + course.getTitle())
                        .description("Spend a little more time to master this topic.")
                        .priority("LOW")
                        .courseId(course.getId())
                        .reason("Your progress in " + course.getTitle() + " is currently " + progress + "%. Spend additional time here to build momentum.")
                        .build());
            }

            // Rule 1 - Next lesson (MEDIUM)
            if (progress < 100.0) {
                // Find first incomplete lesson by orderIndex
                List<Long> completedIds = progresses.stream()
                        .filter(p -> p.getStatus() == LessonProgressStatus.COMPLETED)
                        .map(p -> p.getLesson().getId())
                        .collect(Collectors.toList());

                Optional<Lesson> nextLessonOpt = lessons.stream()
                        .filter(l -> !completedIds.contains(l.getId()))
                        .findFirst();

                if (nextLessonOpt.isPresent()) {
                    Lesson nextLesson = nextLessonOpt.get();
                    list.add(RecommendationDto.builder()
                            .id("rule1_next_lesson_" + nextLesson.getId())
                            .type("NEXT_LESSON")
                            .title("Next Lesson: " + nextLesson.getTitle())
                            .description("Resume your path by starting the next lesson.")
                            .priority("MEDIUM")
                            .courseId(course.getId())
                            .lessonId(nextLesson.getId())
                            .reason("You have completed " + completedCount + " of " + lessons.size() + " lessons in " + course.getTitle() + ". " + nextLesson.getTitle() + " is the next incomplete lesson.")
                            .build());
                }
            }
        }

        // Sort recommendations: HIGH first, then MEDIUM, then LOW
        list.sort(Comparator.comparingInt(this::getPriorityWeight).reversed());
        return list;
    }

    private int getPriorityWeight(RecommendationDto dto) {
        if ("HIGH".equalsIgnoreCase(dto.getPriority())) return 3;
        if ("MEDIUM".equalsIgnoreCase(dto.getPriority())) return 2;
        if ("LOW".equalsIgnoreCase(dto.getPriority())) return 1;
        return 0;
    }
}

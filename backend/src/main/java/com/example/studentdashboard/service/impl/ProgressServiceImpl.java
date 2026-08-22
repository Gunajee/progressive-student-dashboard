package com.example.studentdashboard.service.impl;

import com.example.studentdashboard.dto.LessonProgressDto;
import com.example.studentdashboard.dto.ProgressUpdateDto;
import com.example.studentdashboard.entity.*;
import com.example.studentdashboard.repository.EnrollmentRepository;
import com.example.studentdashboard.repository.LessonProgressRepository;
import com.example.studentdashboard.repository.LessonRepository;
import com.example.studentdashboard.repository.UserRepository;
import com.example.studentdashboard.service.ActivityService;
import com.example.studentdashboard.service.ProgressService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProgressServiceImpl implements ProgressService {

    private final LessonRepository lessonRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final LessonProgressRepository lessonProgressRepository;
    private final UserRepository userRepository;
    private final ActivityService activityService;

    @Override
    @Transactional(readOnly = true)
    public List<LessonProgressDto> getStudentProgress(Long studentId) {
        List<LessonProgress> progressList = lessonProgressRepository.findByStudentId(studentId);
        return progressList.stream()
                .map(this::mapToProgressDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<LessonProgressDto> getCourseProgress(Long studentId, Long courseId) {
        List<LessonProgress> progressList = lessonProgressRepository.findByStudentIdAndLessonCourseId(studentId, courseId);
        return progressList.stream()
                .map(this::mapToProgressDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public LessonProgressDto updateLessonProgress(Long studentId, Long lessonId, ProgressUpdateDto updateDto) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new IllegalArgumentException("Lesson not found"));

        // Verify enrollment
        enrollmentRepository.findByStudentIdAndCourseId(studentId, lesson.getCourse().getId())
                .orElseThrow(() -> new IllegalArgumentException("Student is not enrolled in this course"));

        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));

        Optional<LessonProgress> progressOpt = lessonProgressRepository.findByStudentIdAndLessonId(studentId, lessonId);

        LessonProgress progress;
        boolean isNewlyCompleted = false;

        if (progressOpt.isPresent()) {
            progress = progressOpt.get();
            if (progress.getStatus() != LessonProgressStatus.COMPLETED && updateDto.getStatus() == LessonProgressStatus.COMPLETED) {
                isNewlyCompleted = true;
            }
            // Accumulate time spent
            progress.setTimeSpentMinutes(progress.getTimeSpentMinutes() + updateDto.getTimeSpentMinutes());
            progress.setStatus(updateDto.getStatus());
        } else {
            progress = LessonProgress.builder()
                    .student(student)
                    .lesson(lesson)
                    .status(updateDto.getStatus())
                    .timeSpentMinutes(updateDto.getTimeSpentMinutes())
                    .build();
            if (updateDto.getStatus() == LessonProgressStatus.COMPLETED) {
                isNewlyCompleted = true;
            }
        }

        if (progress.getStatus() == LessonProgressStatus.COMPLETED && progress.getCompletedAt() == null) {
            progress.setCompletedAt(LocalDateTime.now());
        }

        progress = lessonProgressRepository.save(progress);

        // Log activity session event
        if (updateDto.getTimeSpentMinutes() > 0) {
            activityService.logActivity(
                    studentId,
                    lesson.getCourse().getId(),
                    lessonId,
                    ActivityEventType.STUDY_SESSION,
                    updateDto.getTimeSpentMinutes(),
                    "Studied lesson: " + lesson.getTitle()
            );
        }

        // Log lesson complete event if transitioned
        if (isNewlyCompleted) {
            activityService.logActivity(
                    studentId,
                    lesson.getCourse().getId(),
                    lessonId,
                    ActivityEventType.LESSON_COMPLETE,
                    0,
                    "Completed lesson: " + lesson.getTitle()
            );
        }

        return mapToProgressDto(progress);
    }

    private LessonProgressDto mapToProgressDto(LessonProgress progress) {
        return LessonProgressDto.builder()
                .id(progress.getId())
                .lessonId(progress.getLesson().getId())
                .lessonTitle(progress.getLesson().getTitle())
                .status(progress.getStatus())
                .completedAt(progress.getCompletedAt())
                .timeSpentMinutes(progress.getTimeSpentMinutes())
                .build();
    }
}

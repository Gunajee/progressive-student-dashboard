package com.example.studentdashboard.service.impl;

import com.example.studentdashboard.dto.LessonDto;
import com.example.studentdashboard.entity.Course;
import com.example.studentdashboard.entity.Lesson;
import com.example.studentdashboard.entity.LessonProgressStatus;
import com.example.studentdashboard.repository.CourseRepository;
import com.example.studentdashboard.repository.LessonRepository;
import com.example.studentdashboard.service.LessonAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LessonAdminServiceImpl implements LessonAdminService {

    private final LessonRepository lessonRepository;
    private final CourseRepository courseRepository;

    @Override
    @Transactional(readOnly = true)
    public List<LessonDto> getLessonsByCourse(Long courseId) {
        return lessonRepository.findByCourseIdOrderByOrderIndexAsc(courseId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public LessonDto getLessonById(Long lessonId) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new IllegalArgumentException("Lesson not found"));
        return mapToDto(lesson);
    }

    @Override
    @Transactional
    public LessonDto createLesson(Long courseId, LessonDto lessonDto) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found"));

        Lesson lesson = Lesson.builder()
                .course(course)
                .title(lessonDto.getTitle())
                .description(lessonDto.getDescription())
                .content(lessonDto.getContent())
                .orderIndex(lessonDto.getOrderIndex())
                .estimatedMinutes(lessonDto.getEstimatedMinutes())
                .build();
        return mapToDto(lessonRepository.save(lesson));
    }

    @Override
    @Transactional
    public LessonDto updateLesson(Long lessonId, LessonDto lessonDto) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new IllegalArgumentException("Lesson not found"));

        lesson.setTitle(lessonDto.getTitle());
        lesson.setDescription(lessonDto.getDescription());
        lesson.setContent(lessonDto.getContent());
        lesson.setOrderIndex(lessonDto.getOrderIndex());
        lesson.setEstimatedMinutes(lessonDto.getEstimatedMinutes());

        return mapToDto(lessonRepository.save(lesson));
    }

    @Override
    @Transactional
    public void deleteLesson(Long lessonId) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new IllegalArgumentException("Lesson not found"));
        lessonRepository.delete(lesson);
    }

    private LessonDto mapToDto(Lesson lesson) {
        return LessonDto.builder()
                .id(lesson.getId())
                .courseId(lesson.getCourse().getId())
                .title(lesson.getTitle())
                .description(lesson.getDescription())
                .content(lesson.getContent())
                .orderIndex(lesson.getOrderIndex())
                .estimatedMinutes(lesson.getEstimatedMinutes())
                .status(LessonProgressStatus.NOT_STARTED) // default for admin view
                .timeSpentMinutes(0)
                .build();
    }
}

package com.example.studentdashboard.service;

import com.example.studentdashboard.dto.LessonDto;
import java.util.List;

public interface LessonAdminService {
    List<LessonDto> getLessonsByCourse(Long courseId);
    LessonDto getLessonById(Long lessonId);
    LessonDto createLesson(Long courseId, LessonDto lessonDto);
    LessonDto updateLesson(Long lessonId, LessonDto lessonDto);
    void deleteLesson(Long lessonId);
}

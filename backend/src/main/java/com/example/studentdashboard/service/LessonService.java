package com.example.studentdashboard.service;

import com.example.studentdashboard.dto.LessonDto;

import java.util.List;

public interface LessonService {
    List<LessonDto> getLessonsForCourse(Long studentId, Long courseId);
    LessonDto getLessonDetails(Long studentId, Long lessonId);
}

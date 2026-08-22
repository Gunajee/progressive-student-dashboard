package com.example.studentdashboard.service;

import com.example.studentdashboard.dto.LessonProgressDto;
import com.example.studentdashboard.dto.ProgressUpdateDto;

import java.util.List;

public interface ProgressService {
    List<LessonProgressDto> getStudentProgress(Long studentId);
    List<LessonProgressDto> getCourseProgress(Long studentId, Long courseId);
    LessonProgressDto updateLessonProgress(Long studentId, Long lessonId, ProgressUpdateDto updateDto);
}

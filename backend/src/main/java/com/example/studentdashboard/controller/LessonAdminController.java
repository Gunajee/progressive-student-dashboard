package com.example.studentdashboard.controller;

import com.example.studentdashboard.dto.LessonDto;
import com.example.studentdashboard.service.LessonAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/lessons")
@RequiredArgsConstructor
public class LessonAdminController {

    private final LessonAdminService lessonAdminService;

    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<LessonDto>> getLessonsByCourse(@PathVariable Long courseId) {
        return ResponseEntity.ok(lessonAdminService.getLessonsByCourse(courseId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<LessonDto> getLessonById(@PathVariable Long id) {
        return ResponseEntity.ok(lessonAdminService.getLessonById(id));
    }

    @PostMapping("/course/{courseId}")
    public ResponseEntity<LessonDto> createLesson(@PathVariable Long courseId, @RequestBody LessonDto lessonDto) {
        return new ResponseEntity<>(lessonAdminService.createLesson(courseId, lessonDto), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<LessonDto> updateLesson(@PathVariable Long id, @RequestBody LessonDto lessonDto) {
        return ResponseEntity.ok(lessonAdminService.updateLesson(id, lessonDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLesson(@PathVariable Long id) {
        lessonAdminService.deleteLesson(id);
        return ResponseEntity.noContent().build();
    }
}

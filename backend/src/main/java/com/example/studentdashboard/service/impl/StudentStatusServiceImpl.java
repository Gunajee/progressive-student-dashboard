package com.example.studentdashboard.service.impl;

import com.example.studentdashboard.dto.CourseDto;
import com.example.studentdashboard.dto.StudentStatus;
import com.example.studentdashboard.entity.ActivityEvent;
import com.example.studentdashboard.repository.ActivityEventRepository;
import com.example.studentdashboard.service.CourseService;
import com.example.studentdashboard.service.StudentStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StudentStatusServiceImpl implements StudentStatusService {

    private final CourseService courseService;
    private final ActivityEventRepository activityEventRepository;

    @Override
    @Transactional(readOnly = true)
    public StudentStatus calculateStatus(Long studentId) {
        List<CourseDto> enrolledCourses = courseService.getEnrolledCourses(studentId);
        double averageProgress = 0.0;
        if (!enrolledCourses.isEmpty()) {
            averageProgress = enrolledCourses.stream()
                    .mapToDouble(CourseDto::getProgressPercentage)
                    .average()
                    .orElse(0.0);
        }

        List<ActivityEvent> events = activityEventRepository.findByStudentIdOrderByEventDateDesc(studentId);
        boolean inactiveFiveDays = false;
        if (events.isEmpty()) {
            inactiveFiveDays = true;
        } else {
            LocalDateTime lastEventDate = events.get(0).getEventDate();
            long days = ChronoUnit.DAYS.between(lastEventDate, LocalDateTime.now());
            if (days >= 5) {
                inactiveFiveDays = true;
            }
        }

        if (averageProgress < 40.0 || inactiveFiveDays) {
            return StudentStatus.AT_RISK;
        } else if (averageProgress >= 70.0) {
            return StudentStatus.HEALTHY;
        } else {
            return StudentStatus.NEEDS_ATTENTION;
        }
    }
}

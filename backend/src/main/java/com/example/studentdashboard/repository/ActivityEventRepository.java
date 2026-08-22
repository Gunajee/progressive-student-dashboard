package com.example.studentdashboard.repository;

import com.example.studentdashboard.entity.ActivityEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ActivityEventRepository extends JpaRepository<ActivityEvent, Long> {
    List<ActivityEvent> findByStudentIdOrderByEventDateDesc(Long studentId);
    Page<ActivityEvent> findByStudentIdOrderByEventDateDesc(Long studentId, Pageable pageable);
    List<ActivityEvent> findByStudentIdAndEventDateBetween(Long studentId, LocalDateTime start, LocalDateTime end);
}

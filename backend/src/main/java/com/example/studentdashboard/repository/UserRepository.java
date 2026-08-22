package com.example.studentdashboard.repository;

import com.example.studentdashboard.entity.Role;
import com.example.studentdashboard.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    List<User> findByMentorIdAndRole(Long mentorId, Role role);
    Page<User> findByMentorIdAndRole(Long mentorId, Role role, Pageable pageable);
}

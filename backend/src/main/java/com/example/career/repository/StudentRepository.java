package com.example.career.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.career.model.Student;

public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByUserId(Long userId);
    Optional<Student> findByUserEmailIgnoreCase(String email);
}

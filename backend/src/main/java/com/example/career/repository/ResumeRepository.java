package com.example.career.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.career.model.Resume;

public interface ResumeRepository extends JpaRepository<Resume, Long> {
    List<Resume> findByStudentUserIdOrderByUploadedAtDesc(Long userId);
}

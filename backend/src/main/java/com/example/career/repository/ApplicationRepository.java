package com.example.career.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.career.model.Application;

public interface ApplicationRepository extends JpaRepository<Application, Long> {
    List<Application> findByStudentUserIdOrderByAppliedAtDesc(Long userId);
    List<Application> findByJobIdOrderByAppliedAtDesc(Long jobId);
    List<Application> findByJobRecruiterUserEmailIgnoreCaseOrderByAppliedAtDesc(String email);
    long countByJobRecruiterUserEmailIgnoreCase(String email);
    long countByJobRecruiterUserEmailIgnoreCaseAndStatus(String email,
            com.example.career.model.ApplicationStatus status);
    long countByStatus(com.example.career.model.ApplicationStatus status);
    boolean existsByStudentIdAndJobId(Long studentId, Long jobId);
}

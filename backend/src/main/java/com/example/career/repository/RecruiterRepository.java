package com.example.career.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.career.model.Recruiter;

public interface RecruiterRepository extends JpaRepository<Recruiter, Long> {
    Optional<Recruiter> findByUserId(Long userId);
    Optional<Recruiter> findByUserEmailIgnoreCase(String email);
}

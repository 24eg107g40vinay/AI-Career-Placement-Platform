package com.example.career.dto;

import java.time.Instant;
import java.util.List;

import com.example.career.model.ApplicationStatus;

public record ApplicationResponse(
        Long id,
        Long jobId,
        String jobTitle,
        String company,
        Long studentId,
        String studentName,
        String studentEmail,
        String studentEducation,
        List<String> studentSkills,
        ApplicationStatus status,
        Instant appliedAt,
        String coverMessage) {}

package com.example.career.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import com.example.career.model.EmploymentType;

public record JobResponse(
        Long id,
        String title,
        String description,
        String company,
        String location,
        EmploymentType employmentType,
        BigDecimal salary,
        int experience,
        List<String> skills,
        Instant createdAt,
        LocalDate deadline,
        boolean active) {}

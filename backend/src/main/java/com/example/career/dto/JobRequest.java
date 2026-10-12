package com.example.career.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

import com.example.career.model.EmploymentType;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record JobRequest(
        @NotBlank @Size(max = 180) String title,
        @NotBlank @Size(max = 10000) String description,
        @NotBlank @Size(max = 160) String location,
        @NotNull EmploymentType employmentType,
        @DecimalMin("0.0") BigDecimal salary,
        @Min(0) int experience,
        @FutureOrPresent LocalDate deadline,
        @Size(max = 30) Set<@NotBlank @Size(max = 100) String> skills) {}

package com.example.career.dto;

import com.example.career.model.ApplicationStatus;

import jakarta.validation.constraints.NotNull;

public record ApplicationStatusRequest(@NotNull ApplicationStatus status) {}

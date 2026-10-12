package com.example.career.dto;

import java.time.Instant;

import com.example.career.model.Role;

public record AdminUserResponse(Long id, String name, String email, Role role, boolean enabled, Instant createdAt) {}

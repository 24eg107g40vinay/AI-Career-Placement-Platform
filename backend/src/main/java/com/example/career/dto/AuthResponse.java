package com.example.career.dto;

import com.example.career.model.Role;

public record AuthResponse(String token, Long id, String name, String email, Role role) {}

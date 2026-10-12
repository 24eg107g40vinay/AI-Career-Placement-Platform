package com.example.career.dto;

import java.util.List;

public record StudentProfileResponse(
        Long id,
        String name,
        String email,
        String phone,
        String education,
        String college,
        Integer graduationYear,
        String bio,
        String location,
        List<String> skills,
        Integer experienceYears) {}

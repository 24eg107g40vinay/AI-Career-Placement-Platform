package com.example.career.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Size;

public record StudentProfileRequest(
        @Size(max = 32) String phone,
        @Size(max = 160) String education,
        @Size(max = 200) String college,
        @Min(1900) Integer graduationYear,
        @Size(max = 3000) String bio,
        @Size(max = 160) String location,
        @Min(0) @Max(80) Integer experienceYears) {}

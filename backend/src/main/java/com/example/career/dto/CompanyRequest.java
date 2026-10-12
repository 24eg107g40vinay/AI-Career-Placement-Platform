package com.example.career.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CompanyRequest(
        @NotBlank @Size(max = 160) String name,
        @Size(max = 3000) String description,
        @Size(max = 255) String website,
        @Size(max = 160) String location) {}

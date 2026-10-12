package com.example.career.dto;

import java.util.List;

public record JobRecommendationResponse(
        JobResponse job,
        int matchPercentage,
        List<String> matchedSkills,
        List<String> missingSkills) {}

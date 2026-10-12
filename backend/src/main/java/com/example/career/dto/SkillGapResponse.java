package com.example.career.dto;

import java.util.List;

public record SkillGapResponse(
        Long jobId,
        String jobTitle,
        int matchPercentage,
        List<String> requiredSkills,
        List<String> matchedSkills,
        List<String> missingSkills) {}

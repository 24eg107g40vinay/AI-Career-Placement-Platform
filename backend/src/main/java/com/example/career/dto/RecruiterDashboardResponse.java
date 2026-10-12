package com.example.career.dto;

public record RecruiterDashboardResponse(
        long activeJobs,
        long totalApplicants,
        long shortlisted,
        long interviews) {}

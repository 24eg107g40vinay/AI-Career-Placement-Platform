package com.example.career.dto;

public record AdminDashboardResponse(
        long students,
        long recruiters,
        long companies,
        long jobs,
        long applications) {}

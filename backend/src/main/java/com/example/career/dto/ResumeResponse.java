package com.example.career.dto;

import java.time.Instant;

public record ResumeResponse(Long id, String fileName, long fileSizeBytes, Instant uploadedAt) {}

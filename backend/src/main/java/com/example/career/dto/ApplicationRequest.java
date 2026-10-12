package com.example.career.dto;

import jakarta.validation.constraints.Size;

public record ApplicationRequest(@Size(max = 3000) String coverMessage) {}

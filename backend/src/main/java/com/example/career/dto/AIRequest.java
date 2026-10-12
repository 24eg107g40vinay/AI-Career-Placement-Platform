package com.example.career.dto;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.Valid;

public record AIRequest(
        @NotBlank @Size(max = 2000) String message,
        @Size(max = 8) List<@Valid AIConversationTurn> history) {}

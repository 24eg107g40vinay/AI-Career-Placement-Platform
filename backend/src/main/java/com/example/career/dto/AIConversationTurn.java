package com.example.career.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AIConversationTurn(
        @NotNull Role role,
        @NotBlank @Size(max = 2000) String content) {
    public enum Role {
        USER,
        ASSISTANT
    }
}

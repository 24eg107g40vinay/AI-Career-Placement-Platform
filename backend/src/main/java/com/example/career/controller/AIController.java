package com.example.career.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.career.dto.AIRequest;
import com.example.career.dto.AIResponse;
import com.example.career.service.AIService;
import com.example.career.service.StudentService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/ai")
@PreAuthorize("hasRole('STUDENT')")
public class AIController {
    private final AIService aiService;
    private final StudentService studentService;

    public AIController(AIService aiService, StudentService studentService) {
        this.aiService = aiService;
        this.studentService = studentService;
    }

    @PostMapping("/chat")
    public AIResponse chat(@Valid @RequestBody AIRequest request, @AuthenticationPrincipal UserDetails user) {
        var profile = studentService.getProfile(user.getUsername());
        return aiService.chat(request.message().trim(), request.history(), profile);
    }
}

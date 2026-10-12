package com.example.career.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.career.dto.CompanyRequest;
import com.example.career.dto.CompanyResponse;
import com.example.career.dto.RecruiterDashboardResponse;
import com.example.career.service.RecruiterService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/recruiters")
@PreAuthorize("hasRole('RECRUITER')")
public class RecruiterController {
    private final RecruiterService recruiterService;

    public RecruiterController(RecruiterService recruiterService) {
        this.recruiterService = recruiterService;
    }

    @GetMapping("/dashboard")
    public RecruiterDashboardResponse dashboard(@AuthenticationPrincipal UserDetails user) {
        return recruiterService.dashboard(user.getUsername());
    }

    @GetMapping("/company")
    public CompanyResponse getCompany(@AuthenticationPrincipal UserDetails user) {
        return recruiterService.getCompany(user.getUsername());
    }

    @org.springframework.web.bind.annotation.PutMapping("/company")
    public CompanyResponse saveCompany(@AuthenticationPrincipal UserDetails user,
            @Valid @RequestBody CompanyRequest request) {
        return recruiterService.saveCompany(user.getUsername(), request);
    }
}

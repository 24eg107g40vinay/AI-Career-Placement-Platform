package com.example.career.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.career.dto.ApplicationRequest;
import com.example.career.dto.ApplicationResponse;
import com.example.career.dto.ApplicationStatusRequest;
import com.example.career.service.ApplicationService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/applications")
public class ApplicationController {
    private final ApplicationService applicationService;

    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @PostMapping("/{jobId}")
    @PreAuthorize("hasRole('STUDENT')")
    @ResponseStatus(HttpStatus.CREATED)
    public ApplicationResponse apply(@PathVariable Long jobId, @Valid @RequestBody ApplicationRequest request,
            @AuthenticationPrincipal UserDetails user) {
        return applicationService.apply(jobId, user.getUsername(), request.coverMessage());
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('STUDENT')")
    public List<ApplicationResponse> myApplications(@AuthenticationPrincipal UserDetails user) {
        return applicationService.myApplications(user.getUsername());
    }

    @GetMapping("/job/{jobId}")
    @PreAuthorize("hasRole('RECRUITER')")
    public List<ApplicationResponse> applicants(@PathVariable Long jobId,
            @AuthenticationPrincipal UserDetails user) {
        return applicationService.applicants(jobId, user.getUsername());
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('RECRUITER', 'ADMIN')")
    public ApplicationResponse updateStatus(@PathVariable Long id,
            @Valid @RequestBody ApplicationStatusRequest request,
            @AuthenticationPrincipal UserDetails user, Authentication authentication) {
        boolean administrator = authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
        return applicationService.updateStatus(id, request.status(), user.getUsername(), administrator);
    }
}

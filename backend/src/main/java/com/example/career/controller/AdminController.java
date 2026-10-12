package com.example.career.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.career.dto.AdminDashboardResponse;
import com.example.career.dto.AdminUserResponse;
import com.example.career.dto.AdminUserStatusRequest;
import com.example.career.dto.ApplicationResponse;
import com.example.career.dto.CompanyResponse;
import com.example.career.dto.JobResponse;
import com.example.career.model.Role;
import com.example.career.service.AdminService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/dashboard")
    public AdminDashboardResponse dashboard() {
        return adminService.dashboard();
    }

    @GetMapping("/students")
    public List<AdminUserResponse> students(@RequestParam(required = false) String search) {
        return adminService.users(Role.STUDENT, search);
    }

    @GetMapping("/recruiters")
    public List<AdminUserResponse> recruiters(@RequestParam(required = false) String search) {
        return adminService.users(Role.RECRUITER, search);
    }

    @GetMapping("/companies")
    public List<CompanyResponse> companies(@RequestParam(required = false) String search) {
        return adminService.companies(search);
    }

    @GetMapping("/jobs")
    public List<JobResponse> jobs() {
        return adminService.jobs();
    }

    @GetMapping("/applications")
    public List<ApplicationResponse> applications() {
        return adminService.applications();
    }

    @PatchMapping("/users/{id}")
    public AdminUserResponse setUserStatus(@PathVariable Long id, @Valid @RequestBody AdminUserStatusRequest request) {
        return adminService.setUserEnabled(id, request.enabled());
    }

    @DeleteMapping("/jobs/{id}")
    public ResponseEntity<Void> deactivateJob(@PathVariable Long id) {
        adminService.deactivateJob(id);
        return ResponseEntity.noContent().build();
    }
}

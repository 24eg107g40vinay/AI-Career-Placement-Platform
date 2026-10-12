package com.example.career.service;

import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.career.dto.AdminDashboardResponse;
import com.example.career.dto.AdminUserResponse;
import com.example.career.dto.ApplicationResponse;
import com.example.career.dto.CompanyResponse;
import com.example.career.dto.JobResponse;
import com.example.career.exception.InvalidRequestException;
import com.example.career.exception.ResourceNotFoundException;
import com.example.career.model.Role;
import com.example.career.model.User;
import com.example.career.repository.ApplicationRepository;
import com.example.career.repository.CompanyRepository;
import com.example.career.repository.JobRepository;
import com.example.career.repository.UserRepository;

@Service
public class AdminService {
    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final JobRepository jobRepository;
    private final ApplicationRepository applicationRepository;
    private final JobService jobService;
    private final ApplicationService applicationService;

    public AdminService(UserRepository userRepository, CompanyRepository companyRepository,
            JobRepository jobRepository, ApplicationRepository applicationRepository, JobService jobService,
            ApplicationService applicationService) {
        this.userRepository = userRepository;
        this.companyRepository = companyRepository;
        this.jobRepository = jobRepository;
        this.applicationRepository = applicationRepository;
        this.jobService = jobService;
        this.applicationService = applicationService;
    }

    @Transactional(readOnly = true)
    public AdminDashboardResponse dashboard() {
        return new AdminDashboardResponse(
                userRepository.countByRole(Role.STUDENT),
                userRepository.countByRole(Role.RECRUITER),
                companyRepository.count(),
                jobRepository.countByActiveTrue(),
                applicationRepository.count());
    }

    @Transactional(readOnly = true)
    public List<AdminUserResponse> users(Role role, String query) {
        return userRepository.findByRole(role).stream()
                .filter(user -> matches(user, query))
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CompanyResponse> companies(String query) {
        String normalized = normalize(query);
        return companyRepository.findAll().stream()
                .filter(company -> normalized == null
                        || company.getName().toLowerCase(Locale.ROOT).contains(normalized)
                        || (company.getLocation() != null
                                && company.getLocation().toLowerCase(Locale.ROOT).contains(normalized)))
                .map(company -> new CompanyResponse(company.getId(), company.getName(),
                        company.getDescription(), company.getWebsite(), company.getLocation()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<JobResponse> jobs() {
        return jobService.findAllJobs();
    }

    @Transactional(readOnly = true)
    public List<ApplicationResponse> applications() {
        return applicationService.allApplications();
    }

    @Transactional
    public AdminUserResponse setUserEnabled(Long id, boolean enabled) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (user.getRole() == Role.ADMIN && !enabled) {
            throw new InvalidRequestException("Administrator accounts cannot be deactivated from this screen");
        }
        user.setEnabled(enabled);
        return toResponse(user);
    }

    @Transactional
    public void deactivateJob(Long id) {
        jobService.adminDelete(id);
    }

    private boolean matches(User user, String query) {
        String normalized = normalize(query);
        return normalized == null
                || user.getName().toLowerCase(Locale.ROOT).contains(normalized)
                || user.getEmail().toLowerCase(Locale.ROOT).contains(normalized);
    }

    private String normalize(String query) {
        return query == null || query.isBlank() ? null : query.trim().toLowerCase(Locale.ROOT);
    }

    private AdminUserResponse toResponse(User user) {
        return new AdminUserResponse(user.getId(), user.getName(), user.getEmail(),
                user.getRole(), user.isEnabled(), user.getCreatedAt());
    }
}

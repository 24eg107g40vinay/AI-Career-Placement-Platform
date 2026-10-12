package com.example.career.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.career.dto.CompanyRequest;
import com.example.career.dto.CompanyResponse;
import com.example.career.dto.RecruiterDashboardResponse;
import com.example.career.exception.ResourceNotFoundException;
import com.example.career.model.ApplicationStatus;
import com.example.career.model.Company;
import com.example.career.model.Recruiter;
import com.example.career.repository.ApplicationRepository;
import com.example.career.repository.CompanyRepository;
import com.example.career.repository.JobRepository;
import com.example.career.repository.RecruiterRepository;

@Service
public class RecruiterService {
    private final RecruiterRepository recruiterRepository;
    private final CompanyRepository companyRepository;
    private final JobRepository jobRepository;
    private final ApplicationRepository applicationRepository;

    public RecruiterService(RecruiterRepository recruiterRepository, CompanyRepository companyRepository,
            JobRepository jobRepository, ApplicationRepository applicationRepository) {
        this.recruiterRepository = recruiterRepository;
        this.companyRepository = companyRepository;
        this.jobRepository = jobRepository;
        this.applicationRepository = applicationRepository;
    }

    @Transactional(readOnly = true)
    public RecruiterDashboardResponse dashboard(String email) {
        return new RecruiterDashboardResponse(
                jobRepository.countByRecruiterUserEmailIgnoreCaseAndActiveTrue(email),
                applicationRepository.countByJobRecruiterUserEmailIgnoreCase(email),
                applicationRepository.countByJobRecruiterUserEmailIgnoreCaseAndStatus(email, ApplicationStatus.SHORTLISTED),
                applicationRepository.countByJobRecruiterUserEmailIgnoreCaseAndStatus(email, ApplicationStatus.INTERVIEW));
    }

    @Transactional(readOnly = true)
    public CompanyResponse getCompany(String email) {
        Recruiter recruiter = findRecruiter(email);
        return recruiter.getCompany() == null ? null : toResponse(recruiter.getCompany());
    }

    @Transactional
    public CompanyResponse saveCompany(String email, CompanyRequest request) {
        Recruiter recruiter = findRecruiter(email);
        Company company = recruiter.getCompany();
        if (company == null) {
            company = companyRepository.save(new Company(request.name().trim(), request.description(),
                    request.website(), request.location()));
            recruiter.setCompany(company);
        } else {
            company.update(request.name().trim(), request.description(), request.website(), request.location());
        }
        return toResponse(company);
    }

    private Recruiter findRecruiter(String email) {
        return recruiterRepository.findByUserEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("Recruiter profile not found"));
    }

    private CompanyResponse toResponse(Company company) {
        return new CompanyResponse(company.getId(), company.getName(), company.getDescription(),
                company.getWebsite(), company.getLocation());
    }
}

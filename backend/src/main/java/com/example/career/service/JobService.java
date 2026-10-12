package com.example.career.service;

import java.util.List;
import java.util.Locale;
import java.util.LinkedHashSet;
import java.util.Set;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.example.career.dto.JobRequest;
import com.example.career.dto.JobResponse;
import com.example.career.exception.InvalidRequestException;
import com.example.career.exception.ResourceNotFoundException;
import com.example.career.model.EmploymentType;
import com.example.career.model.Job;
import com.example.career.model.Recruiter;
import com.example.career.model.Skill;
import com.example.career.repository.RecruiterRepository;
import com.example.career.repository.JobRepository;
import com.example.career.repository.SkillRepository;

@Service
public class JobService {
    private final JobRepository jobRepository;
    private final RecruiterRepository recruiterRepository;
    private final SkillRepository skillRepository;

    public JobService(JobRepository jobRepository, RecruiterRepository recruiterRepository, SkillRepository skillRepository) {
        this.jobRepository = jobRepository;
        this.recruiterRepository = recruiterRepository;
        this.skillRepository = skillRepository;
    }

    @Transactional(readOnly = true)
    public List<JobResponse> search(String keyword, String location, EmploymentType employmentType, String skill) {
        return jobRepository.searchActiveJobs(normalize(keyword), normalize(location), employmentType, normalize(skill))
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<JobResponse> search(
            String keyword,
            String location,
            String company,
            EmploymentType employmentType,
            String skill,
            Integer minExperience,
            Integer maxExperience,
            BigDecimal minSalary,
            BigDecimal maxSalary,
            Integer postedWithinDays,
            int page,
            int size,
            String sortBy) {
        if (page < 0 || size < 1 || size > 100) {
            throw new InvalidRequestException("Page must be non-negative and size must be between 1 and 100");
        }
        if (minExperience != null && minExperience < 0
                || maxExperience != null && maxExperience < 0
                || minExperience != null && maxExperience != null && minExperience > maxExperience) {
            throw new InvalidRequestException("Experience range is invalid");
        }
        if (minSalary != null && minSalary.signum() < 0
                || maxSalary != null && maxSalary.signum() < 0
                || minSalary != null && maxSalary != null && minSalary.compareTo(maxSalary) > 0) {
            throw new InvalidRequestException("Salary range is invalid");
        }
        if (postedWithinDays != null && (postedWithinDays < 1 || postedWithinDays > 3650)) {
            throw new InvalidRequestException("Posted-within-days must be between 1 and 3650");
        }

        String normalizedSort = sortBy == null ? "RELEVANCE" : sortBy.trim().toUpperCase(Locale.ROOT);
        if (!Set.of("RELEVANCE", "NEWEST", "SALARY_HIGH", "SALARY_LOW").contains(normalizedSort)) {
            throw new InvalidRequestException("Sort must be RELEVANCE, NEWEST, SALARY_HIGH, or SALARY_LOW");
        }
        Instant postedAfter = postedWithinDays == null
                ? null
                : Instant.now().minus(postedWithinDays, ChronoUnit.DAYS);
        Pageable pageable = PageRequest.of(page, size);

        return jobRepository.searchActiveJobs(
                        normalizeKeyword(keyword),
                        normalize(location),
                        normalize(company),
                        employmentType,
                        normalize(skill),
                        minExperience,
                        maxExperience,
                        minSalary,
                        maxSalary,
                        postedAfter,
                        normalizedSort,
                        pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public JobResponse findById(Long id) {
        Job job = jobRepository.findById(id)
                .filter(Job::isActive)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));
        return toResponse(job);
    }

    @Transactional(readOnly = true)
    public Job requireActiveJob(Long id) {
        return jobRepository.findById(id)
                .filter(Job::isActive)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));
    }

    @Transactional(readOnly = true)
    public List<Job> activeJobs() {
        return jobRepository.searchActiveJobs(null, null, null, null);
    }

    @Transactional(readOnly = true)
    public List<JobResponse> findRecruiterJobs(String email) {
        return jobRepository.findByRecruiterUserEmailIgnoreCaseOrderByCreatedAtDesc(email)
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<JobResponse> findAllJobs() {
        return jobRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional
    public void adminDelete(Long id) {
        jobRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"))
                .deactivate();
    }

    @Transactional
    public JobResponse create(JobRequest request, String email) {
        Recruiter recruiter = recruiterRepository.findByUserEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("Recruiter profile not found"));
        Job job = new Job(request.title().trim(), request.description().trim(), request.location().trim(),
                request.employmentType(), request.salary(), request.experience(), recruiter, request.deadline());
        job.getSkills().addAll(resolveSkills(request.skills()));
        return toResponse(jobRepository.save(job));
    }

    @Transactional
    public JobResponse update(Long id, JobRequest request, String email) {
        Job job = ownedJob(id, email);
        job.update(request.title().trim(), request.description().trim(), request.location().trim(),
                request.employmentType(), request.salary(), request.experience(), request.deadline(),
                resolveSkills(request.skills()));
        return toResponse(job);
    }

    @Transactional
    public void delete(Long id, String email) {
        ownedJob(id, email).deactivate();
    }

    private Job ownedJob(Long id, String email) {
        return jobRepository.findByIdAndRecruiterUserEmailIgnoreCase(id, email)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));
    }

    private Set<Skill> resolveSkills(Set<String> names) {
        Set<Skill> skills = new LinkedHashSet<>();
        if (names == null) {
            return skills;
        }
        for (String name : names) {
            String cleanName = name.trim();
            if (!cleanName.isEmpty()) {
                skills.add(skillRepository.findByNameIgnoreCase(cleanName)
                        .orElseGet(() -> skillRepository.save(new Skill(cleanName))));
            }
        }
        return skills;
    }

    private JobResponse toResponse(Job job) {
        String company = job.getRecruiter().getCompany() == null
                ? null
                : job.getRecruiter().getCompany().getName();
        return new JobResponse(
                job.getId(),
                job.getTitle(),
                job.getDescription(),
                company,
                job.getLocation(),
                job.getEmploymentType(),
                job.getSalary(),
                job.getExperience(),
                job.getSkills().stream().map(skill -> skill.getName()).sorted().toList(),
                job.getCreatedAt(),
                job.getDeadline(),
                job.isActive());
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeKeyword(String value) {
        String normalized = normalize(value);
        return normalized == null ? null : normalized.replaceAll("\\s+", "%");
    }
}

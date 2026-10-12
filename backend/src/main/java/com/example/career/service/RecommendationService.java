package com.example.career.service;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.career.dto.JobRecommendationResponse;
import com.example.career.dto.JobResponse;
import com.example.career.dto.SkillGapResponse;
import com.example.career.model.Job;
import com.example.career.model.Skill;

@Service
public class RecommendationService {
    private final JobService jobService;
    private final StudentService studentService;

    public RecommendationService(JobService jobService, StudentService studentService) {
        this.jobService = jobService;
        this.studentService = studentService;
    }

    @Transactional(readOnly = true)
    public List<JobRecommendationResponse> recommendedJobs(String email) {
        Set<String> studentSkills = studentService.skillNames(email);
        return jobService.activeJobs().stream()
                .map(job -> toRecommendation(job, studentSkills))
                .sorted(java.util.Comparator.comparingInt(JobRecommendationResponse::matchPercentage).reversed()
                        .thenComparing(item -> item.job().createdAt(), java.util.Comparator.reverseOrder()))
                .toList();
    }

    @Transactional(readOnly = true)
    public SkillGapResponse skillGap(Long jobId, String email) {
        Set<String> studentSkills = studentService.skillNames(email);
        Job job = jobService.requireActiveJob(jobId);
        List<String> required = job.getSkills().stream().map(Skill::getName).sorted().toList();
        List<String> matched = required.stream()
                .filter(skill -> studentSkills.contains(skill.toLowerCase(Locale.ROOT)))
                .toList();
        List<String> missing = required.stream()
                .filter(skill -> !studentSkills.contains(skill.toLowerCase(Locale.ROOT)))
                .toList();
        int percentage = required.isEmpty() ? 0 : matched.size() * 100 / required.size();
        return new SkillGapResponse(job.getId(), job.getTitle(), percentage, required, matched, missing);
    }

    private JobRecommendationResponse toRecommendation(Job job, Set<String> studentSkills) {
        List<String> required = job.getSkills().stream().map(Skill::getName).sorted().toList();
        List<String> matched = required.stream()
                .filter(skill -> studentSkills.contains(skill.toLowerCase(Locale.ROOT)))
                .toList();
        List<String> missing = required.stream()
                .filter(skill -> !studentSkills.contains(skill.toLowerCase(Locale.ROOT)))
                .toList();
        int percentage = required.isEmpty() ? 0 : matched.size() * 100 / required.size();
        JobResponse response = new JobResponse(
                job.getId(), job.getTitle(), job.getDescription(),
                job.getRecruiter().getCompany() == null ? null : job.getRecruiter().getCompany().getName(),
                job.getLocation(), job.getEmploymentType(), job.getSalary(), job.getExperience(),
                required, job.getCreatedAt(), job.getDeadline(), job.isActive());
        return new JobRecommendationResponse(response, percentage, matched, missing);
    }
}

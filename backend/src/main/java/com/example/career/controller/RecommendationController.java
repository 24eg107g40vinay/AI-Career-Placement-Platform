package com.example.career.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.career.dto.JobRecommendationResponse;
import com.example.career.dto.SkillGapResponse;
import com.example.career.service.RecommendationService;

@RestController
@RequestMapping("/api")
@PreAuthorize("hasRole('STUDENT')")
public class RecommendationController {
    private final RecommendationService recommendationService;

    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @GetMapping("/recommendations/jobs")
    public List<JobRecommendationResponse> recommendedJobs(@AuthenticationPrincipal UserDetails user) {
        return recommendationService.recommendedJobs(user.getUsername());
    }

    @GetMapping("/skill-gap/{jobId}")
    public SkillGapResponse skillGap(@PathVariable Long jobId, @AuthenticationPrincipal UserDetails user) {
        return recommendationService.skillGap(jobId, user.getUsername());
    }
}

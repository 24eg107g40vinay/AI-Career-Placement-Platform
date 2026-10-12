package com.example.career.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.career.dto.SkillRequest;
import com.example.career.dto.StudentProfileRequest;
import com.example.career.dto.StudentProfileResponse;
import com.example.career.model.Skill;
import com.example.career.service.StudentService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/students/profile")
@PreAuthorize("hasRole('STUDENT')")
public class StudentController {
    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @GetMapping
    public StudentProfileResponse getProfile(@AuthenticationPrincipal UserDetails user) {
        return studentService.getProfile(user.getUsername());
    }

    @PutMapping
    public StudentProfileResponse updateProfile(@AuthenticationPrincipal UserDetails user,
            @Valid @RequestBody StudentProfileRequest request) {
        return studentService.updateProfile(user.getUsername(), request);
    }

    @GetMapping("/skills")
    public List<Skill> getSkills(@AuthenticationPrincipal UserDetails user) {
        return studentService.getSkills(user.getUsername());
    }

    @PostMapping("/skills")
    public List<Skill> addSkill(@AuthenticationPrincipal UserDetails user, @Valid @RequestBody SkillRequest request) {
        return studentService.addSkill(user.getUsername(), request);
    }

    @DeleteMapping("/skills/{id}")
    public List<Skill> removeSkill(@AuthenticationPrincipal UserDetails user, @PathVariable Long id) {
        return studentService.removeSkill(user.getUsername(), id);
    }
}

package com.example.career.service;

import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.career.dto.SkillRequest;
import com.example.career.dto.StudentProfileRequest;
import com.example.career.dto.StudentProfileResponse;
import com.example.career.exception.ResourceNotFoundException;
import com.example.career.model.Skill;
import com.example.career.model.Student;
import com.example.career.repository.SkillRepository;
import com.example.career.repository.StudentRepository;

@Service
public class StudentService {
    private final StudentRepository studentRepository;
    private final SkillRepository skillRepository;

    public StudentService(StudentRepository studentRepository, SkillRepository skillRepository) {
        this.studentRepository = studentRepository;
        this.skillRepository = skillRepository;
    }

    @Transactional(readOnly = true)
    public StudentProfileResponse getProfile(String email) {
        return toResponse(findStudent(email));
    }

    @Transactional
    public StudentProfileResponse updateProfile(String email, StudentProfileRequest request) {
        Student student = findStudent(email);
        student.updateProfile(request.phone(), request.education(), request.college(),
                request.graduationYear(), request.bio(), request.location(), request.experienceYears());
        return toResponse(student);
    }

    @Transactional(readOnly = true)
    public List<Skill> getSkills(String email) {
        return List.copyOf(findStudent(email).getSkills());
    }

    @Transactional
    public List<Skill> addSkill(String email, SkillRequest request) {
        Student student = findStudent(email);
        String name = request.name().trim();
        Skill skill = skillRepository.findByNameIgnoreCase(name)
                .orElseGet(() -> skillRepository.save(new Skill(name)));
        boolean alreadyAdded = student.getSkills().stream()
                .anyMatch(existing -> existing.getId().equals(skill.getId()));
        if (!alreadyAdded) {
            student.getSkills().add(skill);
        }
        return List.copyOf(student.getSkills());
    }

    @Transactional
    public List<Skill> removeSkill(String email, Long skillId) {
        Student student = findStudent(email);
        boolean removed = student.getSkills().removeIf(skill -> skill.getId().equals(skillId));
        if (!removed) {
            throw new ResourceNotFoundException("Skill not found in your profile");
        }
        return List.copyOf(student.getSkills());
    }

    @Transactional(readOnly = true)
    public java.util.Set<String> skillNames(String email) {
        return findStudent(email).getSkills().stream()
                .map(skill -> skill.getName().toLowerCase(Locale.ROOT))
                .collect(java.util.stream.Collectors.toSet());
    }

    @Transactional(readOnly = true)
    public Student requireStudent(String email) {
        return findStudent(email);
    }

    private Student findStudent(String email) {
        return studentRepository.findByUserEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("Student profile not found"));
    }

    private StudentProfileResponse toResponse(Student student) {
        return new StudentProfileResponse(
                student.getId(),
                student.getUser().getName(),
                student.getUser().getEmail(),
                student.getPhone(),
                student.getEducation(),
                student.getCollege(),
                student.getGraduationYear(),
                student.getBio(),
                student.getLocation(),
                student.getSkills().stream().map(Skill::getName).sorted().toList(),
                student.getExperienceYears());
    }
}

package com.example.career.service;

import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Sort;

import com.example.career.dto.ApplicationResponse;
import com.example.career.exception.DuplicateApplicationException;
import com.example.career.exception.ResourceNotFoundException;
import com.example.career.model.Application;
import com.example.career.model.ApplicationStatus;
import com.example.career.model.Job;
import com.example.career.model.Student;
import com.example.career.model.User;
import com.example.career.repository.ApplicationRepository;
import com.example.career.repository.JobRepository;
import com.example.career.repository.StudentRepository;
import com.example.career.repository.UserRepository;

@Service
public class ApplicationService {
    private final ApplicationRepository applicationRepository;
    private final JobService jobService;
    private final StudentService studentService;
    private final UserRepository userRepository;
    private final JobRepository jobRepository;
    private final StudentRepository studentRepository;

    public ApplicationService(ApplicationRepository applicationRepository, JobService jobService,
            StudentService studentService, UserRepository userRepository, JobRepository jobRepository,
            StudentRepository studentRepository) {
        this.applicationRepository = applicationRepository;
        this.jobService = jobService;
        this.studentService = studentService;
        this.userRepository = userRepository;
        this.jobRepository = jobRepository;
        this.studentRepository = studentRepository;
    }

    @Transactional
    public ApplicationResponse apply(Long jobId, String email, String coverMessage) {
        Student student = studentService.requireStudent(email);
        Job job = jobService.requireActiveJob(jobId);
        if (applicationRepository.existsByStudentIdAndJobId(student.getId(), jobId)) {
            throw new DuplicateApplicationException();
        }
        return toResponse(applicationRepository.save(new Application(student, job, coverMessage)));
    }

    @Transactional(readOnly = true)
    public List<ApplicationResponse> myApplications(String email) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
        return applicationRepository.findByStudentUserIdOrderByAppliedAtDesc(user.getId())
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<ApplicationResponse> allApplications() {
        return applicationRepository.findAll(Sort.by(Sort.Direction.DESC, "appliedAt"))
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<ApplicationResponse> applicants(Long jobId, String email) {
        Job job = jobRepository.findByIdAndRecruiterUserEmailIgnoreCase(jobId, email)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));
        return applicationRepository.findByJobIdOrderByAppliedAtDesc(job.getId())
                .stream().map(this::toResponse).toList();
    }

    @Transactional
    public ApplicationResponse updateStatus(Long applicationId, ApplicationStatus status, String email,
            boolean administrator) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found"));
        if (!administrator && !application.getJob().getRecruiter().getUser().getEmail().equalsIgnoreCase(email)) {
            throw new AccessDeniedException("You may only update applications for your jobs");
        }
        application.updateStatus(status);
        return toResponse(application);
    }

    private ApplicationResponse toResponse(Application application) {
        Student student = application.getStudent();
        String company = application.getJob().getRecruiter().getCompany() == null
                ? null
                : application.getJob().getRecruiter().getCompany().getName();
        return new ApplicationResponse(
                application.getId(),
                application.getJob().getId(),
                application.getJob().getTitle(),
                company,
                student.getId(),
                student.getUser().getName(),
                student.getUser().getEmail(),
                student.getEducation(),
                student.getSkills().stream().map(skill -> skill.getName()).sorted().toList(),
                application.getStatus(),
                application.getAppliedAt(),
                application.getCoverMessage());
    }
}

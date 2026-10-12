package com.example.career.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.career.dto.ResumeResponse;
import com.example.career.exception.InvalidRequestException;
import com.example.career.exception.ResourceNotFoundException;
import com.example.career.model.Resume;
import com.example.career.model.Student;
import com.example.career.repository.ResumeRepository;

@Service
public class ResumeService {
    private final ResumeRepository resumeRepository;
    private final StudentService studentService;
    private final Path uploadDirectory;
    private final long maximumSize;

    public ResumeService(ResumeRepository resumeRepository, StudentService studentService,
            @Value("${app.upload-dir}") String uploadDirectory,
            @Value("${app.resume.max-size-bytes}") long maximumSize) {
        this.resumeRepository = resumeRepository;
        this.studentService = studentService;
        this.uploadDirectory = Path.of(uploadDirectory).toAbsolutePath().normalize();
        this.maximumSize = maximumSize;
    }

    @Transactional(readOnly = true)
    public List<ResumeResponse> list(String email) {
        return resumeRepository.findByStudentUserIdOrderByUploadedAtDesc(
                        studentService.requireStudent(email).getUser().getId())
                .stream().map(this::toResponse).toList();
    }

    @Transactional
    public ResumeResponse upload(String email, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidRequestException("Choose a PDF resume to upload");
        }
        if (file.getSize() > maximumSize) {
            throw new InvalidRequestException("Resume must be 5 MB or smaller");
        }
        String originalName = file.getOriginalFilename() == null ? "" : file.getOriginalFilename();
        String safeName = Path.of(originalName.replace('\\', '/')).getFileName().toString();
        if (!safeName.toLowerCase(java.util.Locale.ROOT).endsWith(".pdf")
                || !MediaType.APPLICATION_PDF_VALUE.equalsIgnoreCase(file.getContentType())) {
            throw new InvalidRequestException("Only PDF resumes are allowed");
        }

        Student student = studentService.requireStudent(email);
        String storedName = UUID.randomUUID() + ".pdf";
        Path target = uploadDirectory.resolve(storedName).normalize();
        if (!target.startsWith(uploadDirectory)) {
            throw new InvalidRequestException("Invalid resume filename");
        }

        try {
            Files.createDirectories(uploadDirectory);
            byte[] contents = file.getBytes();
            byte[] signature = java.util.Arrays.copyOf(contents, Math.min(5, contents.length));
            if (!"%PDF-".equals(new String(signature, java.nio.charset.StandardCharsets.US_ASCII))) {
                throw new InvalidRequestException("The uploaded file is not a valid PDF");
            }
            Files.write(target, contents);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to store the uploaded resume", exception);
        }

        Resume resume = resumeRepository.save(new Resume(student, safeName, target.toString()));
        return new ResumeResponse(resume.getId(), resume.getFileName(), file.getSize(), resume.getUploadedAt());
    }

    @Transactional(readOnly = true)
    public DownloadedResume download(String email, Long id) {
        Resume resume = ownedResume(id, email);
        Path file = Path.of(resume.getFilePath()).toAbsolutePath().normalize();
        if (!file.startsWith(uploadDirectory)) {
            throw new IllegalStateException("Stored resume path is outside the configured upload directory");
        }
        try {
            Resource resource = new UrlResource(file.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new ResourceNotFoundException("Resume file not found");
            }
            return new DownloadedResume(resume.getFileName(), resource);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to read the stored resume", exception);
        }
    }

    @Transactional
    public void delete(String email, Long id) {
        Resume resume = ownedResume(id, email);
        Path file = Path.of(resume.getFilePath()).toAbsolutePath().normalize();
        if (!file.startsWith(uploadDirectory)) {
            throw new IllegalStateException("Stored resume path is outside the configured upload directory");
        }
        try {
            Files.deleteIfExists(file);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to delete the stored resume file", exception);
        }
        resumeRepository.delete(resume);
    }

    private Resume ownedResume(Long id, String email) {
        Student student = studentService.requireStudent(email);
        return resumeRepository.findById(id)
                .filter(resume -> resume.getStudent().getId().equals(student.getId()))
                .orElseThrow(() -> new ResourceNotFoundException("Resume not found"));
    }

    private ResumeResponse toResponse(Resume resume) {
        long size = 0;
        try {
            size = Files.size(Path.of(resume.getFilePath()));
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to read the stored resume size", exception);
        }
        return new ResumeResponse(resume.getId(), resume.getFileName(), size, resume.getUploadedAt());
    }

    public record DownloadedResume(String fileName, Resource resource) {}
}

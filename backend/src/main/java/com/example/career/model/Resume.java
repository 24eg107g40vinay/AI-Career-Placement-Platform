package com.example.career.model;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "resumes")
public class Resume {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @NotBlank
    @Size(max = 255)
    @Column(nullable = false, length = 255)
    private String fileName;

    @NotBlank
    @Size(max = 1000)
    @Column(nullable = false, length = 1000)
    private String filePath;

    @Column(nullable = false, updatable = false)
    private Instant uploadedAt;

    protected Resume() {}

    public Resume(Student student, String fileName, String filePath) {
        this.student = student;
        this.fileName = fileName;
        this.filePath = filePath;
    }

    @PrePersist
    void setUploadedAt() {
        if (uploadedAt == null) {
            uploadedAt = Instant.now();
        }
    }

    public Long getId() { return id; }
    public Student getStudent() { return student; }
    public String getFileName() { return fileName; }
    public String getFilePath() { return filePath; }
    public Instant getUploadedAt() { return uploadedAt; }
}

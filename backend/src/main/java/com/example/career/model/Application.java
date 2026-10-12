package com.example.career.model;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "applications", uniqueConstraints = {
        @UniqueConstraint(name = "uk_application_student_job", columnNames = { "student_id", "job_id" })
})
public class Application {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ApplicationStatus status = ApplicationStatus.APPLIED;

    @Column(nullable = false, updatable = false)
    private Instant appliedAt;

    @Column(length = 3000)
    private String coverMessage;

    protected Application() {}

    public Application(Student student, Job job) {
        this.student = student;
        this.job = job;
    }

    public Application(Student student, Job job, String coverMessage) {
        this.student = student;
        this.job = job;
        this.coverMessage = coverMessage;
    }

    @PrePersist
    void setAppliedAt() {
        if (appliedAt == null) {
            appliedAt = Instant.now();
        }
    }

    public Long getId() { return id; }
    public Student getStudent() { return student; }
    public Job getJob() { return job; }
    public ApplicationStatus getStatus() { return status; }
    public Instant getAppliedAt() { return appliedAt; }
    public String getCoverMessage() { return coverMessage; }

    public void updateStatus(ApplicationStatus status) {
        this.status = status;
    }
}

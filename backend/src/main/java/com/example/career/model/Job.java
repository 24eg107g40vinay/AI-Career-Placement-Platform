package com.example.career.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "jobs")
public class Job {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 180)
    @Column(nullable = false, length = 180)
    private String title;

    @NotBlank
    @Size(max = 10000)
    @Column(nullable = false, length = 10000)
    private String description;

    @NotBlank
    @Size(max = 160)
    @Column(nullable = false, length = 160)
    private String location;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EmploymentType employmentType;

    @Min(0)
    private BigDecimal salary;

    @Min(0)
    @Column(nullable = false)
    private int experience;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recruiter_id", nullable = false)
    private Recruiter recruiter;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "job_skills",
            joinColumns = @JoinColumn(name = "job_id"),
            inverseJoinColumns = @JoinColumn(name = "skill_id"))
    private Set<Skill> skills = new HashSet<>();

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    private LocalDate deadline;

    @Column(nullable = false)
    private boolean active = true;

    protected Job() {}

    public Job(String title, String description, String location, EmploymentType employmentType,
            BigDecimal salary, int experience, Recruiter recruiter, LocalDate deadline) {
        this.title = title;
        this.description = description;
        this.location = location;
        this.employmentType = employmentType;
        this.salary = salary;
        this.experience = experience;
        this.recruiter = recruiter;
        this.deadline = deadline;
    }

    @PrePersist
    void setCreatedAt() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getLocation() { return location; }
    public EmploymentType getEmploymentType() { return employmentType; }
    public BigDecimal getSalary() { return salary; }
    public int getExperience() { return experience; }
    public Recruiter getRecruiter() { return recruiter; }
    public Set<Skill> getSkills() { return skills; }
    public Instant getCreatedAt() { return createdAt; }
    public LocalDate getDeadline() { return deadline; }
    public boolean isActive() { return active; }

    public void update(String title, String description, String location, EmploymentType employmentType,
            BigDecimal salary, int experience, LocalDate deadline, Set<Skill> skills) {
        this.title = title;
        this.description = description;
        this.location = location;
        this.employmentType = employmentType;
        this.salary = salary;
        this.experience = experience;
        this.deadline = deadline;
        this.skills.clear();
        this.skills.addAll(skills);
    }

    public void deactivate() {
        this.active = false;
    }
}

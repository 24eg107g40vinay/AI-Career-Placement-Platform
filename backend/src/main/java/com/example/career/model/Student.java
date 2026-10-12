package com.example.career.model;

import java.util.HashSet;
import java.util.Collection;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "students")
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Size(max = 32)
    private String phone;

    @Size(max = 160)
    private String education;

    @Size(max = 200)
    private String college;

    @Min(1900)
    private Integer graduationYear;

    @Min(0)
    @Max(80)
    private Integer experienceYears;

    @Size(max = 3000)
    @Column(length = 3000)
    private String bio;

    @Size(max = 160)
    private String location;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "student_skills",
            joinColumns = @JoinColumn(name = "student_id"),
            inverseJoinColumns = @JoinColumn(name = "skill_id"))
    private Set<Skill> skills = new HashSet<>();

    protected Student() {}

    public Student(User user) {
        this.user = user;
    }

    public Long getId() { return id; }
    public User getUser() { return user; }
    public String getPhone() { return phone; }
    public String getEducation() { return education; }
    public String getCollege() { return college; }
    public Integer getGraduationYear() { return graduationYear; }
    public Integer getExperienceYears() { return experienceYears; }
    public String getBio() { return bio; }
    public String getLocation() { return location; }
    public Set<Skill> getSkills() { return skills; }

    public void updateProfile(String phone, String education, String college, Integer graduationYear,
            String bio, String location, Integer experienceYears) {
        this.phone = phone;
        this.education = education;
        this.college = college;
        this.graduationYear = graduationYear;
        this.bio = bio;
        this.location = location;
        this.experienceYears = experienceYears;
    }

    public void replaceSkills(Collection<Skill> skills) {
        this.skills.clear();
        this.skills.addAll(skills);
    }
}

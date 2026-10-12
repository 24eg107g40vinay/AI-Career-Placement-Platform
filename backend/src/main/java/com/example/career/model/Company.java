package com.example.career.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "companies")
public class Company {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 160)
    @Column(nullable = false, length = 160)
    private String name;

    @Size(max = 3000)
    @Column(length = 3000)
    private String description;

    @Size(max = 255)
    private String website;

    @Size(max = 160)
    private String location;

    protected Company() {}

    public Company(String name, String description, String website, String location) {
        this.name = name;
        this.description = description;
        this.website = website;
        this.location = location;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getWebsite() { return website; }
    public String getLocation() { return location; }

    public void update(String name, String description, String website, String location) {
        this.name = name;
        this.description = description;
        this.website = website;
        this.location = location;
    }
}

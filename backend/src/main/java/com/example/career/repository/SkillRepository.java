package com.example.career.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.career.model.Skill;

public interface SkillRepository extends JpaRepository<Skill, Long> {
    Optional<Skill> findByNameIgnoreCase(String name);
}

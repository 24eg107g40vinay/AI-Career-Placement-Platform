package com.example.career.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.career.model.Company;

public interface CompanyRepository extends JpaRepository<Company, Long> {}

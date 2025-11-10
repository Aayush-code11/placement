package com.example.placement.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.placement.model.Company;

public interface CompanyRepository extends JpaRepository<Company, Long> {
}

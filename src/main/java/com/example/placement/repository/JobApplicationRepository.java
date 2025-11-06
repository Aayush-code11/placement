package com.example.placement.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.placement.model.JobApplication;

public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {
	List<JobApplication> findByStudentId(Long studentId);
	List<JobApplication> findByCompanyId(Long companyId);
	boolean existsByStudentIdAndCompanyId(Long studentId, Long companyId);
	long countByStudentIdAndStatus(Long studentId, String status);
}

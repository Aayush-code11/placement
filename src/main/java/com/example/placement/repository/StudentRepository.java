package com.example.placement.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.placement.model.Student;

public interface StudentRepository extends JpaRepository<Student, Long> {
	Optional<Student> findByUserId(Long userId);
}

package com.example.placement;

import java.io.File;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.example.placement.repository.CompanyRepository;
import com.example.placement.repository.JobApplicationRepository;
import com.example.placement.repository.StudentRepository;
import com.example.placement.repository.UserRepository;

@SpringBootApplication
public class PlacementApplication {
    public static void main(String[] args) {
        SpringApplication.run(PlacementApplication.class, args);
    }

    @Bean
    CommandLineRunner initData(UserRepository userRepo, StudentRepository studentRepo, CompanyRepository companyRepo, JobApplicationRepository appRepo, org.springframework.security.crypto.password.PasswordEncoder passwordEncoder) {
        return args -> {
            // ensure uploads dir exists but DO NOT create sample data automatically.
            File uploads = new File("uploads");
            if (!uploads.exists()) uploads.mkdirs();
            // IMPORTANT: Removed sample data insertion so users must register themselves.
        };
    }
}

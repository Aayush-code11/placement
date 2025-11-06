package com.example.placement.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.placement.model.JobApplication;
import com.example.placement.model.Student;
import com.example.placement.repository.JobApplicationRepository;
import com.example.placement.repository.StudentRepository;

@Controller
public class ApplicationController {

    private final JobApplicationRepository applicationRepo;
    private final StudentRepository studentRepo;

    public ApplicationController(JobApplicationRepository applicationRepo, StudentRepository studentRepo) {
        this.applicationRepo = applicationRepo;
        this.studentRepo = studentRepo;
    }

    @GetMapping("/api/applications")
    @ResponseBody
    public List<JobApplication> listApplications() { return applicationRepo.findAll(); }

    @PostMapping("/apply")
    public String applyForJob(@RequestParam Long studentId, @RequestParam Long companyId) {
        Optional<Student> sOpt = studentRepo.findById(studentId);
        // companyId validity is checked in repository layer via foreign key when saving application
        if (sOpt.isPresent()) {
            // Check if already applied
            if (applicationRepo.existsByStudentIdAndCompanyId(studentId, companyId)) {
                return "redirect:/student.html?already-applied";
            }
            JobApplication app = new JobApplication();
            app.setStudent(sOpt.get());
            app.setStatus("Applied");
            applicationRepo.save(app);
            return "redirect:/student.html?applied";
        }
        return "redirect:/student.html?error";
    }

    @PostMapping("/api/applications/{id}/status")
    @ResponseBody
    public Object updateApplicationStatus(@PathVariable Long id, @RequestParam String status) {
        Optional<JobApplication> appOpt = applicationRepo.findById(id);
        if (appOpt.isEmpty()) {
            return java.util.Map.of("error", "Application not found");
        }
        JobApplication app = appOpt.get();
        app.setStatus(status);
        applicationRepo.save(app);
        return java.util.Map.of("success", true, "message", "Status updated to " + status);
    }
}

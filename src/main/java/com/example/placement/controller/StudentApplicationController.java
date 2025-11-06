package com.example.placement.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.placement.model.Company;
import com.example.placement.model.JobApplication;
import com.example.placement.model.Student;
import com.example.placement.model.User;
import com.example.placement.repository.CompanyRepository;
import com.example.placement.repository.JobApplicationRepository;
import com.example.placement.repository.StudentRepository;
import com.example.placement.repository.UserRepository;

@Controller
public class StudentApplicationController {

    private final StudentRepository studentRepo;
    private final CompanyRepository companyRepo;
    private final JobApplicationRepository applicationRepo;
    private final UserRepository userRepo;

    public StudentApplicationController(StudentRepository studentRepo, CompanyRepository companyRepo, JobApplicationRepository applicationRepo, UserRepository userRepo) {
        this.studentRepo = studentRepo;
        this.companyRepo = companyRepo;
        this.applicationRepo = applicationRepo;
        this.userRepo = userRepo;
    }

    @PostMapping("/api/student/apply")
    @ResponseBody
    public Object applyForJobAsLoggedInStudent(@RequestParam Long companyId) {
        var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return java.util.Map.of("error", "Not authenticated");
        String username = auth.getName();
        Optional<User> userOpt = userRepo.findByUsername(username);
        if (userOpt.isEmpty()) return java.util.Map.of("error", "User not found");

        Optional<Student> myStudentOpt = studentRepo.findByUserId(userOpt.get().getId());
        if (myStudentOpt.isEmpty()) return java.util.Map.of("error", "Student profile not found");
        Student myStudent = myStudentOpt.get();

        Optional<Company> cOpt = companyRepo.findById(companyId);
        if (cOpt.isEmpty()) return java.util.Map.of("error", "Company not found");

        if (applicationRepo.existsByStudentIdAndCompanyId(myStudent.getId(), companyId)) {
            return java.util.Map.of("error", "Already applied to this company");
        }

        JobApplication app = new JobApplication();
        app.setStudent(myStudent);
        app.setCompany(cOpt.get());
        app.setStatus("Applied");
        applicationRepo.save(app);
        return java.util.Map.of("success", true, "applicationId", app.getId());
    }

    @GetMapping("/api/student/eligible-companies")
    @ResponseBody
    public Object getEligibleCompanies() {
        var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return List.of();
        String username = auth.getName();
        Optional<User> userOpt = userRepo.findByUsername(username);
        if (userOpt.isEmpty()) return List.of();

        Optional<Student> myStudentOpt = studentRepo.findByUserId(userOpt.get().getId());
        if (myStudentOpt.isEmpty()) return List.of();
        Student myStudent = myStudentOpt.get();

        double studentCgpa = myStudent.getCgpa() == null ? 0.0 : myStudent.getCgpa();
        String studentBranch = myStudent.getBranch() == null ? "" : myStudent.getBranch();

        List<Company> allCompanies = companyRepo.findAll();
        List<Company> eligible = new java.util.ArrayList<>();
        for (Company c : allCompanies) {
            boolean cgpaOk = c.getMinCgpa() == null || studentCgpa >= c.getMinCgpa();
            boolean branchOk = c.getBranches() == null || c.getBranches().isEmpty() || c.getBranches().toLowerCase().contains(studentBranch.toLowerCase());
            if (cgpaOk && branchOk) eligible.add(c);
        }
        return eligible;
    }

    @GetMapping("/api/student/my-applications")
    @ResponseBody
    public Object getMyApplications() {
        var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return List.of();
        String username = auth.getName();
        Optional<User> userOpt = userRepo.findByUsername(username);
        if (userOpt.isEmpty()) return List.of();
        Optional<Student> myStudentOpt = studentRepo.findByUserId(userOpt.get().getId());
        if (myStudentOpt.isEmpty()) return List.of();
        return applicationRepo.findByStudentId(myStudentOpt.get().getId());
    }
}

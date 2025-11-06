package com.example.placement.controller;

import java.util.ArrayList;
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
import com.example.placement.repository.CompanyRepository;
import com.example.placement.repository.JobApplicationRepository;
import com.example.placement.repository.StudentRepository;
import com.example.placement.repository.UserRepository;

@Controller
public class CompanyApplicationController {

    private final CompanyRepository companyRepo;
    private final StudentRepository studentRepo;
    private final JobApplicationRepository applicationRepo;
    private final UserRepository userRepo;

    public CompanyApplicationController(CompanyRepository companyRepo, StudentRepository studentRepo, JobApplicationRepository applicationRepo, UserRepository userRepo) {
        this.companyRepo = companyRepo;
        this.studentRepo = studentRepo;
        this.applicationRepo = applicationRepo;
        this.userRepo = userRepo;
    }

    @GetMapping("/api/company/eligible-students")
    @ResponseBody
    public List<Student> getEligibleStudents() {
        var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return List.of();
        String username = auth.getName();
        Optional<com.example.placement.model.User> userOpt = userRepo.findByUsername(username);
        if (userOpt.isEmpty()) return List.of();
        Optional<Company> myCompanyOpt = companyRepo.findByUserId(userOpt.get().getId());
        if (myCompanyOpt.isEmpty()) return List.of();

    Company myCompany = myCompanyOpt.get();
    Double minCgpaD = myCompany.getMinCgpa();
    double minCgpa = minCgpaD == null ? 0.0 : minCgpaD.doubleValue();
        String allowedBranches = myCompany.getBranches() == null ? "" : myCompany.getBranches().toLowerCase();

        List<Student> all = studentRepo.findAll();
        List<Student> eligible = new ArrayList<>();
        for (Student s : all) {
            Double scgpaD = s.getCgpa();
            double scgpa = scgpaD == null ? 0.0 : scgpaD.doubleValue();
            String branch = s.getBranch() == null ? "" : s.getBranch().toLowerCase();
            boolean okCgpa = scgpa >= minCgpa;
            boolean okBranch = allowedBranches.isEmpty() || allowedBranches.contains(branch);
            if (okCgpa && okBranch) eligible.add(s);
        }
        return eligible;
    }

    @GetMapping("/api/company/applications")
    @ResponseBody
    public List<JobApplication> getCompanyApplications() {
        var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return List.of();
        String username = auth.getName();
        Optional<com.example.placement.model.User> userOpt = userRepo.findByUsername(username);
        if (userOpt.isEmpty()) return List.of();
        Optional<Company> myCompanyOpt = companyRepo.findByUserId(userOpt.get().getId());
        if (myCompanyOpt.isEmpty()) return List.of();
        return applicationRepo.findByCompanyId(myCompanyOpt.get().getId());
    }

    @PostMapping("/api/company/update-application-status")
    @ResponseBody
    public Object updateApplicationStatus(@RequestParam Long applicationId, @RequestParam String status) {
        var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return java.util.Map.of("error", "Not authenticated");
        String username = auth.getName();
        Optional<com.example.placement.model.User> userOpt = userRepo.findByUsername(username);
        if (userOpt.isEmpty()) return java.util.Map.of("error", "User not found");
        Optional<Company> myCompanyOpt = companyRepo.findByUserId(userOpt.get().getId());
        if (myCompanyOpt.isEmpty()) return java.util.Map.of("error", "Company profile not found");

        Optional<JobApplication> appOpt = applicationRepo.findById(applicationId);
        if (appOpt.isEmpty()) return java.util.Map.of("error", "Application not found");
        JobApplication app = appOpt.get();
        if (app.getCompany() == null || !app.getCompany().getId().equals(myCompanyOpt.get().getId())) {
            return java.util.Map.of("error", "Unauthorized - not your application");
        }
        app.setStatus(status);
        applicationRepo.save(app);
        return java.util.Map.of("success", true, "status", status);
    }
}

package com.example.placement.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
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

@Controller
public class AdminController {

    private final JobApplicationRepository applicationRepo;
    private final StudentRepository studentRepo;
    private final CompanyRepository companyRepo;

    public AdminController(JobApplicationRepository applicationRepo, StudentRepository studentRepo, CompanyRepository companyRepo) {
        this.applicationRepo = applicationRepo;
        this.studentRepo = studentRepo;
        this.companyRepo = companyRepo;
    }

    @GetMapping("/api/admin/check")
    @ResponseBody
    public Object checkAdminAuth() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return java.util.Map.of("isAdmin", false);
        }
        boolean isAdmin = auth.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .anyMatch(r -> "ADMIN".equalsIgnoreCase(r));
        return java.util.Map.of("isAdmin", isAdmin, "username", auth.getName());
    }

    @GetMapping("/api/companies")
    @ResponseBody
    public List<Company> getAllCompanies() {
        return companyRepo.findAll();
    }

    @PostMapping("/api/companies")
    @ResponseBody
    public Company addCompany(
        @RequestParam String name,
        @RequestParam(required = false) String hrEmail,
        @RequestParam String role,
        @RequestParam(required = false) String ctc,
        @RequestParam(required = false) String location,
        @RequestParam(required = false) Double minCgpa,
        @RequestParam(required = false) String branches,
        @RequestParam(required = false) Integer openings,
        @RequestParam(required = false) String jobDescription
    ) {
        Company company = new Company();
        company.setName(name);
        company.setHrEmail(hrEmail);
        company.setJobRole(role);
        company.setCtc(ctc);
        company.setLocation(location);
        company.setMinCgpa(minCgpa);
        company.setEligibleBranches(branches);
        company.setOpenings(openings);
        company.setJobDescription(jobDescription);
        return companyRepo.save(company);
    }

    @GetMapping("/api/applications")
    @ResponseBody
    public List<JobApplication> getAllApplications() {
        return applicationRepo.findAll();
    }

    @GetMapping("/admin/export/students.csv")
    public void exportStudentsCsv(jakarta.servlet.http.HttpServletResponse resp) throws IOException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getAuthorities().stream().map(GrantedAuthority::getAuthority).noneMatch(r -> "ADMIN".equalsIgnoreCase(r))) {
            resp.setStatus(403);
            resp.getWriter().write("forbidden");
            return;
        }

        resp.setContentType("text/csv");
        resp.setHeader("Content-Disposition", "attachment; filename=students.csv");
        resp.getWriter().write("id,name,roll,branch,cgpa,email,placed\n");

        for (Student s : studentRepo.findAll()) {
            long placed = applicationRepo.findAll().stream()
                .filter(a -> a.getStudent()!=null && a.getStudent().getId().equals(s.getId()) && "Selected".equalsIgnoreCase(a.getStatus()))
                .count();
            double cgpa = s.getCgpa() != null ? s.getCgpa() : 0.0;
            long id = s.getId() != null ? s.getId() : 0L;
            String line = String.format("%d,%s,%s,%s,%.2f,%s,%d\n",
                id, escapeCsv(s.getName()), escapeCsv(s.getRoll()), escapeCsv(s.getBranch()), cgpa, escapeCsv(s.getEmail()), placed);
            resp.getWriter().write(line);
        }
    }

    private String escapeCsv(String v) { if (v==null) return ""; return v.replace(",",";"); }
}

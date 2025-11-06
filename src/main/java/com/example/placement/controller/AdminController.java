package com.example.placement.controller;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.placement.model.Student;
import com.example.placement.repository.JobApplicationRepository;
import com.example.placement.repository.StudentRepository;

import jakarta.servlet.http.HttpSession;

@Controller
public class AdminController {

    private final JobApplicationRepository applicationRepo;
    private final StudentRepository studentRepo;

    public AdminController(JobApplicationRepository applicationRepo, StudentRepository studentRepo) {
        this.applicationRepo = applicationRepo;
        this.studentRepo = studentRepo;
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

    // Export students CSV - admin only
    @GetMapping("/admin/export/students.csv")
    public void exportStudentsCsv(jakarta.servlet.http.HttpServletResponse resp, HttpSession session) throws IOException {
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
            double cgpa = s.getCgpa() == null ? 0.0 : s.getCgpa();
            long id = s.getId() == null ? 0L : s.getId();
            String line = String.format("%d,%s,%s,%s,%.2f,%s,%d\n",
                id, escapeCsv(s.getName()), escapeCsv(s.getRoll()), escapeCsv(s.getBranch()), cgpa, escapeCsv(s.getEmail()), placed);
            resp.getWriter().write(line);
        }
    }

    private String escapeCsv(String v) { if (v==null) return ""; return v.replace(",",";"); }
}

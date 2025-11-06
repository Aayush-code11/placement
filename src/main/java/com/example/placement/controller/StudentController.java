package com.example.placement.controller;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.example.placement.model.Student;
import com.example.placement.model.User;
import com.example.placement.repository.StudentRepository;
import com.example.placement.repository.UserRepository;

import jakarta.servlet.http.HttpSession;

@Controller
public class StudentController {

    private final StudentRepository studentRepo;
    private final UserRepository userRepo;

    public StudentController(StudentRepository studentRepo, UserRepository userRepo) {
        this.studentRepo = studentRepo;
        this.userRepo = userRepo;
    }

    @GetMapping("/api/students")
    @ResponseBody
    public List<Student> listStudents() {
        return studentRepo.findAll();
    }

    @PostMapping("/students")
    public String createStudent(@RequestParam String name,
                                @RequestParam String roll,
                                @RequestParam String branch,
                                @RequestParam Double cgpa,
                                @RequestParam String email,
                                @RequestPart(value = "resume", required = false) MultipartFile resume,
                                HttpSession session) {
        Student s = new Student();
        s.setName(name);
        s.setRoll(roll);
        s.setBranch(branch);
        s.setCgpa(cgpa);
        s.setEmail(email);
        if (cgpa < 0 || cgpa > 10) {
            return "redirect:/student.html?error=cgpa";
        }
        if (resume != null && !resume.isEmpty()) {
            try {
                File uploads = new File("uploads");
                if (!uploads.exists()) uploads.mkdirs();
                String fname = System.currentTimeMillis() + "_" + resume.getOriginalFilename();
                File dest = new File(uploads, fname);
                resume.transferTo(dest);
                s.setResumeFileName(fname);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        studentRepo.save(s);
        return "redirect:/student.html?created";
    }

    @GetMapping("/api/student/me")
    @ResponseBody
    public Object getCurrentStudent() {
        // simple safety: find student linked to logged-in user
        org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return java.util.Map.of("error", "Not authenticated");
        String username = auth.getName();
        Optional<User> userOpt = userRepo.findByUsername(username);
        if (userOpt.isEmpty()) return java.util.Map.of("error", "User not found");
        Optional<Student> myStudentOpt = studentRepo.findByUserId(userOpt.get().getId());
        if (myStudentOpt.isEmpty()) return java.util.Map.of("error", "Student profile not found", "username", username);
        return myStudentOpt.get();
    }
}

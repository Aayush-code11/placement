package com.example.placement.controller;

import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.placement.model.Student;
import com.example.placement.model.User;
import com.example.placement.repository.StudentRepository;
import com.example.placement.repository.UserRepository;

@Controller
public class AuthController {

    private final UserRepository userRepo;
    private final StudentRepository studentRepo;

    public AuthController(UserRepository userRepo, StudentRepository studentRepo) {
        this.userRepo = userRepo;
        this.studentRepo = studentRepo;
    }

    @GetMapping({"/login","/login.html"})
    public String loginPage() { return "login.html"; }

    @GetMapping({"/register","/register.html"})
    public String registerPage() { return "register.html"; }

    @PostMapping("/api/register")
    @ResponseBody
    public Object register(@RequestParam String username, @RequestParam String password, @RequestParam String role) {
        if (userRepo.findByUsername(username).isPresent()) {
            return Map.of("error","User exists");
        }
        User u = new User();
        u.setUsername(username);
        u.setPassword(password); // plaintext for now (existing app uses same)
        u.setRole(role);
        userRepo.save(u);
        if ("STUDENT".equalsIgnoreCase(role)) {
            Student s = new Student(); s.setName(username); s.setEmail(username+"@example.com"); s.setRoll(""); s.setBranch(""); s.setCgpa(0.0);
            studentRepo.save(s);
        }
            return Map.of("success", true);
        }
    }

package com.example.placement.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.placement.model.Student;
import com.example.placement.model.User;
import com.example.placement.repository.StudentRepository;
import com.example.placement.repository.UserRepository;

@RestController
@RequestMapping("/api")
public class AuthController {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UserRepository userRepository, StudentRepository studentRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        
        String username = request.get("username");
        String email = request.get("email");
        String password = request.get("password");
        String role = request.get("role");

        if (userRepository.findByUsername(username).isPresent()) {
            response.put("success", false);
            response.put("error", "Username already exists");
            return response;
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);
        userRepository.save(user);

        if ("STUDENT".equals(role)) {
            Student student = new Student();
            student.setName(username);
            student.setEmail(email);
            student.setRoll("");
            student.setBranch("");
            student.setCgpa(0.0);
            studentRepository.save(student);
        }

        response.put("success", true);
        response.put("message", "Registration successful");
        return response;
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        
        String username = request.get("username");
        String password = request.get("password");

        User user = userRepository.findByUsername(username).orElse(null);
        
        if (user == null) {
            response.put("success", false);
            response.put("error", "User not found");
            return response;
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            response.put("success", false);
            response.put("error", "Invalid password");
            return response;
        }

        response.put("success", true);
        response.put("role", user.getRole());
        response.put("username", user.getUsername());
        return response;
    }
}

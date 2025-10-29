package com.example.placement.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.placement.repository.UserRepository;

@Controller
public class AuthController {

    private final UserRepository userRepo;

    public AuthController(UserRepository userRepo) { this.userRepo = userRepo; }

    @GetMapping("/login")
    public String loginPage() { return "login.html"; }
}

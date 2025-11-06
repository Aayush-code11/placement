package com.example.placement.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Small entry controller - most endpoints have been split into focused controllers
 * (StudentController, CompanyController, ApplicationController, AdminController, AuthController).
 * Keeping this class minimal so each file stays small for review/presentation.
 */
@Controller
public class MainController {

    @GetMapping("/")
    public String home() {
        return "index.html";
    }

    @GetMapping("/health")
    @ResponseBody
    public String health() {
        return "OK";
    }
}

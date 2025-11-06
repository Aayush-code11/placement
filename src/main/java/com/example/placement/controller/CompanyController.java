package com.example.placement.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.placement.model.Company;
import com.example.placement.repository.CompanyRepository;
import com.example.placement.repository.UserRepository;

@Controller
public class CompanyController {

    private final CompanyRepository companyRepo;
    private final UserRepository userRepo;

    public CompanyController(CompanyRepository companyRepo, UserRepository userRepo) {
        this.companyRepo = companyRepo;
        this.userRepo = userRepo;
    }

    @GetMapping("/api/companies")
    @ResponseBody
    public List<Company> listCompanies() { return companyRepo.findAll(); }

    @PostMapping("/companies")
    public String createCompany(@RequestParam String name,
                                @RequestParam String hrEmail,
                                @RequestParam String role,
                                @RequestParam Double minCgpa,
                                @RequestParam String branches,
                                @RequestParam(required = false) String ctc,
                                @RequestParam(required = false) String location,
                                @RequestParam(required = false) String jobDescription,
                                @RequestParam(required = false) Integer openings) {
        Company c = new Company();
        c.setName(name);
        c.setHrEmail(hrEmail);
        c.setRole(role);
        c.setMinCgpa(minCgpa);
        c.setBranches(branches);
        c.setCtc(ctc);
        c.setLocation(location);
        c.setJobDescription(jobDescription);
        c.setOpenings(openings);
        companyRepo.save(c);
        return "redirect:/company.html?created";
    }

    @GetMapping("/api/company/me")
    @ResponseBody
    public Object getCurrentCompany() {
        var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return java.util.Map.of("error", "Not authenticated");
        String username = auth.getName();
        Optional<com.example.placement.model.User> userOpt = userRepo.findByUsername(username);
        if (userOpt.isEmpty()) return java.util.Map.of("error", "User not found");
        Optional<Company> myCompanyOpt = companyRepo.findByUserId(userOpt.get().getId());
        if (myCompanyOpt.isEmpty()) return java.util.Map.of("error", "Company profile not found", "username", username);
        return myCompanyOpt.get();
    }
}

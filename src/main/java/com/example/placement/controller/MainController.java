package com.example.placement.controller;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.example.placement.model.Company;
import com.example.placement.model.JobApplication;
import com.example.placement.model.Student;
import com.example.placement.model.User;
import com.example.placement.repository.CompanyRepository;
import com.example.placement.repository.JobApplicationRepository;
import com.example.placement.repository.StudentRepository;

import jakarta.servlet.http.HttpSession;

@Controller
public class MainController {

    private final StudentRepository studentRepo;
    private final CompanyRepository companyRepo;
    private final JobApplicationRepository applicationRepo;
    private final com.example.placement.repository.UserRepository userRepo;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    public MainController(StudentRepository studentRepo, CompanyRepository companyRepo, JobApplicationRepository applicationRepo,
                          com.example.placement.repository.UserRepository userRepo,
                          org.springframework.security.crypto.password.PasswordEncoder passwordEncoder) {
        this.studentRepo = studentRepo;
        this.companyRepo = companyRepo;
        this.applicationRepo = applicationRepo;
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
    }

    // static pages are served from /static automatically, but provide simple JSON endpoints for debug
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
                                @RequestParam(value = "resume", required = false) MultipartFile resume,
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

    @GetMapping("/api/companies")
    @ResponseBody
    public List<Company> listCompanies() {
        return companyRepo.findAll();
    }

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

    @PostMapping("/apply")
    public String applyForJob(@RequestParam Long studentId, @RequestParam Long companyId) {
        Optional<Student> sOpt = studentRepo.findById(studentId);
        Optional<Company> cOpt = companyRepo.findById(companyId);
        if (sOpt.isPresent() && cOpt.isPresent()) {
            // Check if already applied
            List<JobApplication> existing = applicationRepo.findAll();
            for (JobApplication app : existing) {
                if (app.getStudent() != null && app.getCompany() != null &&
                    app.getStudent().getId().equals(studentId) && 
                    app.getCompany().getId().equals(companyId)) {
                    return "redirect:/student.html?already-applied";
                }
            }
            
            JobApplication app = new JobApplication();
            app.setStudent(sOpt.get());
            app.setCompany(cOpt.get());
            app.setStatus("Applied");
            applicationRepo.save(app);
            return "redirect:/student.html?applied";
        }
        return "redirect:/student.html?error";
    }
    
    // Easy apply for logged-in student (no need to enter student ID)
    @PostMapping("/api/student/apply")
    @ResponseBody
    public Object applyForJobAsLoggedInStudent(@RequestParam Long companyId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return java.util.Map.of("error", "Not authenticated");
        }
        String username = auth.getName();
        Optional<User> userOpt = userRepo.findByUsername(username);
        if (userOpt.isEmpty()) {
            return java.util.Map.of("error", "User not found");
        }
        User user = userOpt.get();
        
        List<Student> students = studentRepo.findAll();
        Student myStudent = null;
        for (Student s : students) {
            if (s.getUserId() != null && s.getUserId().equals(user.getId())) {
                myStudent = s;
                break;
            }
        }
        if (myStudent == null) {
            return java.util.Map.of("error", "Student profile not found");
        }
        
        Optional<Company> cOpt = companyRepo.findById(companyId);
        if (cOpt.isEmpty()) {
            return java.util.Map.of("error", "Company not found");
        }
        
        // Check if already applied
        List<JobApplication> existing = applicationRepo.findAll();
        for (JobApplication app : existing) {
            if (app.getStudent() != null && app.getCompany() != null &&
                app.getStudent().getId().equals(myStudent.getId()) && 
                app.getCompany().getId().equals(companyId)) {
                return java.util.Map.of("error", "Already applied to this company");
            }
        }
        
        JobApplication app = new JobApplication();
        app.setStudent(myStudent);
        app.setCompany(cOpt.get());
        app.setStatus("Applied");
        applicationRepo.save(app);
        
        return java.util.Map.of("success", true, "message", "Application submitted successfully", "applicationId", app.getId());
    }

    @GetMapping("/api/applications")
    @ResponseBody
    public List<JobApplication> listApplications() { return applicationRepo.findAll(); }

    // Check if current user is Admin
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

    // Update Application Status (Admin only)
    @PostMapping("/api/applications/{id}/status")
    @ResponseBody
    public Object updateApplicationStatus(@PathVariable Long id, @RequestParam String status) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return java.util.Map.of("error", "Not authenticated");
        }
        boolean isAdmin = auth.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .anyMatch(r -> "ADMIN".equalsIgnoreCase(r));
        if (!isAdmin) {
            return java.util.Map.of("error", "Admin access required");
        }
        
        Optional<JobApplication> appOpt = applicationRepo.findById(id);
        if (appOpt.isEmpty()) {
            return java.util.Map.of("error", "Application not found");
        }
        
        JobApplication app = appOpt.get();
        app.setStatus(status);
        applicationRepo.save(app);
        
        return java.util.Map.of("success", true, "message", "Status updated to " + status);
    }

    // Get current logged in student details
    @GetMapping("/api/student/me")
    @ResponseBody
    public Object getCurrentStudent() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return java.util.Map.of("error", "Not authenticated");
        }
        String username = auth.getName();
        Optional<User> userOpt = userRepo.findByUsername(username);
        if (userOpt.isEmpty()) {
            return java.util.Map.of("error", "User not found");
        }
        User user = userOpt.get();
        
        // Find student by userId
        List<Student> students = studentRepo.findAll();
        Student myStudent = null;
        for (Student s : students) {
            if (s.getUserId() != null && s.getUserId().equals(user.getId())) {
                myStudent = s;
                break;
            }
        }
        if (myStudent == null) {
            return java.util.Map.of("error", "Student profile not found", "username", username, "userId", user.getId());
        }
        return myStudent;
    }

    // Get eligible companies for current student
    @GetMapping("/api/student/eligible-companies")
    @ResponseBody
    public Object getEligibleCompanies() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return java.util.List.of();
        }
        String username = auth.getName();
        Optional<User> userOpt = userRepo.findByUsername(username);
        if (userOpt.isEmpty()) {
            return java.util.List.of();
        }
        User user = userOpt.get();
        
        List<Student> students = studentRepo.findAll();
        Student myStudent = null;
        for (Student s : students) {
            if (s.getUserId() != null && s.getUserId().equals(user.getId())) {
                myStudent = s;
                break;
            }
        }
        if (myStudent == null) {
            return java.util.List.of();
        }
        
        // Filter companies based on CGPA and branch
        Double studentCgpa = myStudent.getCgpa() == null ? 0.0 : myStudent.getCgpa();
        String studentBranch = myStudent.getBranch() == null ? "" : myStudent.getBranch();
        
        List<Company> allCompanies = companyRepo.findAll();
        List<Company> eligible = new java.util.ArrayList<>();
        for (Company c : allCompanies) {
            boolean cgpaOk = c.getMinCgpa() == null || studentCgpa >= c.getMinCgpa();
            boolean branchOk = c.getBranches() == null || c.getBranches().isEmpty() || 
                              c.getBranches().toLowerCase().contains(studentBranch.toLowerCase());
            if (cgpaOk && branchOk) {
                eligible.add(c);
            }
        }
        return eligible;
    }

    // Get applications for current student
    @GetMapping("/api/student/my-applications")
    @ResponseBody
    public Object getMyApplications() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return java.util.List.of();
        }
        String username = auth.getName();
        Optional<User> userOpt = userRepo.findByUsername(username);
        if (userOpt.isEmpty()) {
            return java.util.List.of();
        }
        User user = userOpt.get();
        
        List<Student> students = studentRepo.findAll();
        Student myStudent = null;
        for (Student s : students) {
            if (s.getUserId() != null && s.getUserId().equals(user.getId())) {
                myStudent = s;
                break;
            }
        }
        if (myStudent == null) {
            return java.util.List.of();
        }
        
        // Get all applications for this student
        List<JobApplication> allApps = applicationRepo.findAll();
        List<JobApplication> myApps = new java.util.ArrayList<>();
        for (JobApplication app : allApps) {
            if (app.getStudent() != null && app.getStudent().getId().equals(myStudent.getId())) {
                myApps.add(app);
            }
        }
        return myApps;
    }

    // ============ COMPANY ENDPOINTS ============
    
    // Get current logged in company details
    @GetMapping("/api/company/me")
    @ResponseBody
    public Object getCurrentCompany() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return java.util.Map.of("error", "Not authenticated");
        }
        String username = auth.getName();
        Optional<User> userOpt = userRepo.findByUsername(username);
        if (userOpt.isEmpty()) {
            return java.util.Map.of("error", "User not found");
        }
        User user = userOpt.get();
        
        // Find company by userId
        List<Company> companies = companyRepo.findAll();
        Company myCompany = null;
        for (Company c : companies) {
            if (c.getUserId() != null && c.getUserId().equals(user.getId())) {
                myCompany = c;
                break;
            }
        }
        if (myCompany == null) {
            return java.util.Map.of("error", "Company profile not found", "username", username, "userId", user.getId());
        }
        return myCompany;
    }
    
    // Get eligible students for current company
    @GetMapping("/api/company/eligible-students")
    @ResponseBody
    public Object getEligibleStudents() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return java.util.List.of();
        }
        String username = auth.getName();
        Optional<User> userOpt = userRepo.findByUsername(username);
        if (userOpt.isEmpty()) {
            return java.util.List.of();
        }
        User user = userOpt.get();
        
        List<Company> companies = companyRepo.findAll();
        Company myCompany = null;
        for (Company c : companies) {
            if (c.getUserId() != null && c.getUserId().equals(user.getId())) {
                myCompany = c;
                break;
            }
        }
        if (myCompany == null) {
            return java.util.List.of();
        }
        
        // Filter students based on company's criteria
        Double minCgpa = myCompany.getMinCgpa() == null ? 0.0 : myCompany.getMinCgpa();
        String allowedBranches = myCompany.getBranches() == null ? "" : myCompany.getBranches();
        
        List<Student> allStudents = studentRepo.findAll();
        List<Student> eligible = new java.util.ArrayList<>();
        for (Student s : allStudents) {
            Double studentCgpa = s.getCgpa() == null ? 0.0 : s.getCgpa();
            String studentBranch = s.getBranch() == null ? "" : s.getBranch();
            
            boolean cgpaOk = studentCgpa >= minCgpa;
            boolean branchOk = allowedBranches.isEmpty() || 
                              allowedBranches.toLowerCase().contains(studentBranch.toLowerCase());
            if (cgpaOk && branchOk) {
                eligible.add(s);
            }
        }
        return eligible;
    }
    
    // Get applications received by current company
    @GetMapping("/api/company/applications")
    @ResponseBody
    public Object getCompanyApplications() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return java.util.List.of();
        }
        String username = auth.getName();
        Optional<User> userOpt = userRepo.findByUsername(username);
        if (userOpt.isEmpty()) {
            return java.util.List.of();
        }
        User user = userOpt.get();
        
        List<Company> companies = companyRepo.findAll();
        Company myCompany = null;
        for (Company c : companies) {
            if (c.getUserId() != null && c.getUserId().equals(user.getId())) {
                myCompany = c;
                break;
            }
        }
        if (myCompany == null) {
            return java.util.List.of();
        }
        
        // Get all applications for this company
        List<JobApplication> allApps = applicationRepo.findAll();
        List<JobApplication> myApps = new java.util.ArrayList<>();
        for (JobApplication app : allApps) {
            if (app.getCompany() != null && app.getCompany().getId().equals(myCompany.getId())) {
                myApps.add(app);
            }
        }
        return myApps;
    }
    
    // Update application status
    @PostMapping("/api/company/update-application-status")
    @ResponseBody
    public Object updateApplicationStatus(@RequestParam Long applicationId, @RequestParam String status) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return java.util.Map.of("error", "Not authenticated");
        }
        
        Optional<JobApplication> appOpt = applicationRepo.findById(applicationId);
        if (appOpt.isEmpty()) {
            return java.util.Map.of("error", "Application not found");
        }
        
        JobApplication app = appOpt.get();
        
        // Verify this application belongs to current company
        String username = auth.getName();
        Optional<User> userOpt = userRepo.findByUsername(username);
        if (userOpt.isEmpty()) {
            return java.util.Map.of("error", "User not found");
        }
        User user = userOpt.get();
        
        List<Company> companies = companyRepo.findAll();
        Company myCompany = null;
        for (Company c : companies) {
            if (c.getUserId() != null && c.getUserId().equals(user.getId())) {
                myCompany = c;
                break;
            }
        }
        
        if (myCompany == null || app.getCompany() == null || !app.getCompany().getId().equals(myCompany.getId())) {
            return java.util.Map.of("error", "Unauthorized - not your application");
        }
        
        // Update status
        app.setStatus(status);
        applicationRepo.save(app);
        
        return java.util.Map.of("success", true, "message", "Status updated to " + status);
    }

    @PostMapping("/register")
    public String registerUser(@RequestParam String username,
                               @RequestParam String password,
                               @RequestParam String role,
                               // student fields
                               @RequestParam(required = false) String name,
                               @RequestParam(required = false) String roll,
                               @RequestParam(required = false) String branch,
                               @RequestParam(required = false) Double cgpa,
                               @RequestParam(required = false) String email,
                               // company fields
                               @RequestParam(required = false) String companyName,
                               @RequestParam(required = false) String hrEmail,
                               @RequestParam(required = false) String roleTitle,
                               @RequestParam(required = false) Double minCgpa,
                               @RequestParam(required = false) String branches,
                               @RequestParam(required = false) String ctc,
                               @RequestParam(required = false) String location,
                               @RequestParam(required = false) String jobDescription,
                               @RequestParam(required = false) Integer openings) {
        // simple check
        if (userRepo.findByUsername(username).isPresent()) {
            return "redirect:/register.html?error=exists";
        }
        User u = new User();
        u.setUsername(username);
        u.setPassword(passwordEncoder.encode(password));
        u.setRole(role==null?"STUDENT":role.toUpperCase());
        userRepo.save(u);
        
        // if registering a student, also create student record
        if ("STUDENT".equalsIgnoreCase(u.getRole())) {
            Student s = new Student();
            s.setUserId(u.getId()); // Link to user
            s.setName(name==null?username:name);
            s.setRoll(roll==null?"R"+System.currentTimeMillis():roll);
            s.setBranch(branch==null?"CSE":branch);
            s.setCgpa(cgpa==null?0.0:cgpa);
            s.setEmail(email==null?"":email);
            studentRepo.save(s);
        }
        
        // if registering a company, create a Company record as well
        if ("COMPANY".equalsIgnoreCase(u.getRole())) {
            Company c = new Company();
            c.setUserId(u.getId()); // Link to user
            c.setName(companyName==null?username:companyName);
            c.setHrEmail(hrEmail==null?email:hrEmail);
            c.setRole(roleTitle==null?"Recruiter":roleTitle);
            c.setMinCgpa(minCgpa==null?0.0:minCgpa);
            c.setBranches(branches==null?"":branches);
            c.setCtc(ctc==null?"Not disclosed":ctc);
            c.setLocation(location==null?"":location);
            c.setJobDescription(jobDescription==null?"":jobDescription);
            c.setOpenings(openings==null?0:openings);
            companyRepo.save(c);
        }
        return "redirect:/login.html?registered";
    }

    @GetMapping("/resume/{studentId}")
    public void downloadResume(@PathVariable Long studentId, jakarta.servlet.http.HttpServletResponse resp, HttpSession session) throws IOException {
        // access control via Spring Security authorities
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) { resp.setStatus(403); resp.getWriter().write("unauthorized"); return; }
        boolean allowed = auth.getAuthorities().stream().map(GrantedAuthority::getAuthority).anyMatch(r -> "COMPANY".equalsIgnoreCase(r) || "ADMIN".equalsIgnoreCase(r));
        if (!allowed) { resp.setStatus(403); resp.getWriter().write("forbidden"); return; }
        Student s = studentRepo.findById(studentId).orElse(null);
        if (s == null || s.getResumeFileName() == null) { resp.setStatus(404); resp.getWriter().write("not found"); return; }
        File f = new File("uploads", s.getResumeFileName());
        if (!f.exists()) { resp.setStatus(404); resp.getWriter().write("file not found"); return; }
        resp.setContentType("application/pdf");
        resp.setHeader("Content-Disposition", "attachment; filename=resume_"+s.getRoll()+".pdf");
        java.nio.file.Files.copy(f.toPath(), resp.getOutputStream());
        resp.getOutputStream().flush();
    }

    @GetMapping("/admin/export/students.csv")
    public void exportStudentsCsv(jakarta.servlet.http.HttpServletResponse resp, HttpSession session) throws IOException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getAuthorities().stream().map(GrantedAuthority::getAuthority).noneMatch(r -> "ADMIN".equalsIgnoreCase(r))) { resp.setStatus(403); resp.getWriter().write("forbidden"); return; }
        resp.setContentType("text/csv");
        resp.setHeader("Content-Disposition", "attachment; filename=students.csv");
        String header = "id,name,roll,branch,cgpa,email,placed\n";
        resp.getWriter().write(header);
        for (Student s : studentRepo.findAll()) {
            long placed = applicationRepo.findAll().stream().filter(a -> a.getStudent()!=null && a.getStudent().getId().equals(s.getId()) && "Selected".equalsIgnoreCase(a.getStatus())).count();
            String line = String.format("%d,%s,%s,%s,%.2f,%s,%d\n", s.getId(), escapeCsv(s.getName()), escapeCsv(s.getRoll()), escapeCsv(s.getBranch()), s.getCgpa()==null?0.0:s.getCgpa(), escapeCsv(s.getEmail()), placed);
            resp.getWriter().write(line);
        }
    }

    private String escapeCsv(String v) { if (v==null) return ""; return v.replaceAll(",",";"); }
}

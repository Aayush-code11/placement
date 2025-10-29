package com.example.placement.security;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class RoleBasedAuthSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        for (GrantedAuthority ga : authentication.getAuthorities()) {
            String role = ga.getAuthority();
            if ("ADMIN".equalsIgnoreCase(role)) {
                response.sendRedirect("/admin.html");
                return;
            }
            if ("COMPANY".equalsIgnoreCase(role)) {
                response.sendRedirect("/company.html");
                return;
            }
        }
        response.sendRedirect("/student.html");
    }
}

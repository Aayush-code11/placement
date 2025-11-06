package com.example.placement.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider(UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider auth = new DaoAuthenticationProvider();
        auth.setUserDetailsService(userDetailsService);
        auth.setPasswordEncoder(passwordEncoder);
        return auth;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, AuthenticationSuccessHandler successHandler) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // disabled for demo; enable and add CSRF tokens for production
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/css/**", "/js/**", "/", "/index.html", "/login.html", "/register", "/register.html").permitAll()
                .requestMatchers("/api/students", "/api/companies", "/api/applications").permitAll()
                // only admin can update application statuses
                .requestMatchers(HttpMethod.POST, "/api/applications/*/status").hasAuthority("ADMIN")
                .requestMatchers("/api/student/**").hasAuthority("STUDENT")
                .requestMatchers("/api/admin/**").hasAuthority("ADMIN")
                .requestMatchers("/admin.html", "/admin/**").hasAuthority("ADMIN")
                .requestMatchers("/student.html").hasAuthority("STUDENT")
                .requestMatchers("/company.html").hasAuthority("COMPANY")
                .requestMatchers("/resume/**").hasAnyAuthority("ADMIN","COMPANY")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login.html")
                .loginProcessingUrl("/doLogin")
                .successHandler(successHandler)
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/")
            );
        return http.build();
    }
}

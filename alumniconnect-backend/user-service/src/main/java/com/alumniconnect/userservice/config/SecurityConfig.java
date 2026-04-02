package com.alumniconnect.userservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // 🔐 Password Encoder
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 🔐 Security Configuration
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                // ❌ Disable CSRF (required for APIs)
                .csrf(csrf -> csrf.disable())

                // ❌ Disable default login mechanisms
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())

                // 🔓 Authorization rules
                .authorizeHttpRequests(auth -> auth
                        // ✅ Allow these endpoints WITHOUT authentication
                        .requestMatchers("/auth/register", "/auth/login").permitAll()

                        // 🔒 Everything else requires authentication
                        .anyRequest().authenticated()
                )

                // ❌ No session (JWT-based system)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                );

        return http.build();
    }
}
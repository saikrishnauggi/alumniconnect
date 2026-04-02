package com.alumniconnect.notificationservice.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.security.Key;
import java.util.Collections;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${jwt.secret}")
    private String secret;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/notifications/health").permitAll()
                        // Internal POST from other services
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/notifications").permitAll()
                        .anyRequest().authenticated())
                .addFilterBefore(jwtFilter(), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    private OncePerRequestFilter jwtFilter() {
        return new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(HttpServletRequest request,
                                            HttpServletResponse response, FilterChain chain)
                    throws ServletException, IOException {

                String userId = request.getHeader("X-User-Id");
                String role   = request.getHeader("X-User-Role");

                // 🔐 Fix: Set SecurityContext for Gateway headers
                if (userId != null) {
                    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                            userId, null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + (role != null ? role : "USER")))
                    );
                    SecurityContextHolder.getContext().setAuthentication(auth);
                    chain.doFilter(request, response);
                    return;
                }

                // Fallback for direct JWT (Dev mode)
                String authHeader = request.getHeader("Authorization");
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    try {
                        String token = authHeader.substring(7);
                        Key key = Keys.hmacShaKeyFor(secret.getBytes());
                        Claims claims = Jwts.parserBuilder().setSigningKey(key)
                                .build().parseClaimsJws(token).getBody();

                        String jwtUserId = String.valueOf(claims.get("userId"));
                        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                                jwtUserId, null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
                        );
                        SecurityContextHolder.getContext().setAuthentication(auth);
                    } catch (Exception ignored) {}
                }
                chain.doFilter(request, response);
            }
        };
    }
}
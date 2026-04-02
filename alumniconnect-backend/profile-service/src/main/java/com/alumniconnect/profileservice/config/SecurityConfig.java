/*
package com.alumniconnect.profileservice.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.security.Key;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${jwt.secret}")
    private String secret;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(c -> c.disable())
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/profiles/health").permitAll()
                .requestMatchers("/profiles/alumni/**").permitAll()
                .anyRequest().authenticated())
            .addFilterBefore(jwtFilter(), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public OncePerRequestFilter jwtFilter() {
        return new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(HttpServletRequest request,
                    HttpServletResponse response, FilterChain chain)
                    throws ServletException, IOException {

                // Trust headers set by API Gateway
                String userId = request.getHeader("X-User-Id");
                String role   = request.getHeader("X-User-Role");

                if (userId != null && role != null) {
                    // Headers already validated by gateway — mark as authenticated
                    chain.doFilter(request, response);
                    return;
                }

                // Fallback: validate JWT directly (for dev/testing without gateway)
                String authHeader = request.getHeader("Authorization");
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    try {
                        String token = authHeader.substring(7);
                        Key key = Keys.hmacShaKeyFor(secret.getBytes());
                        Claims claims = Jwts.parserBuilder().setSigningKey(key)
                                .build().parseClaimsJws(token).getBody();

                        // Mutate request with synthetic headers so controllers work the same
                        jakarta.servlet.http.HttpServletRequestWrapper wrapper =
                            new jakarta.servlet.http.HttpServletRequestWrapper(request) {
                                @Override
                                public String getHeader(String name) {
                                    if ("X-User-Id".equals(name))
                                        return String.valueOf(claims.get("userId"));
                                    if ("X-User-Role".equals(name))
                                        return (String) claims.get("role");
                                    return super.getHeader(name);
                                }
                            };
                        chain.doFilter(wrapper, response);
                        return;
                    } catch (Exception ignored) {}
                }

                chain.doFilter(request, response);
            }
        };
    }
}
*/

package com.alumniconnect.profileservice.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
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
                        .requestMatchers("/profiles/health").permitAll()
                        .requestMatchers("/profiles/alumni/**").permitAll()
                        .anyRequest().authenticated())
                // Call the method directly to avoid double-registration as a Bean
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

                if (userId != null && role != null) {
                    // 🔐 CRITICAL: Tell Spring Security the user is authenticated
                    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                            userId, null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role))
                    );
                    SecurityContextHolder.getContext().setAuthentication(auth);

                    chain.doFilter(request, response);
                    return;
                }

                // Fallback: validate JWT directly (for dev/testing)
                String authHeader = request.getHeader("Authorization");
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    try {
                        String token = authHeader.substring(7);
                        Key key = Keys.hmacShaKeyFor(secret.getBytes());
                        Claims claims = Jwts.parserBuilder().setSigningKey(key)
                                .build().parseClaimsJws(token).getBody();

                        String jwtUserId = String.valueOf(claims.get("userId"));
                        String jwtRole = (String) claims.get("role");

                        // Set Authentication for direct JWT access too
                        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                                jwtUserId, null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + jwtRole))
                        );
                        SecurityContextHolder.getContext().setAuthentication(auth);

                        chain.doFilter(request, response);
                        return;
                    } catch (Exception ignored) {}
                }

                chain.doFilter(request, response);
            }
        };
    }
}
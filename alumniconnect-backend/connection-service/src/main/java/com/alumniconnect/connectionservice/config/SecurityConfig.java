/*
package com.alumniconnect.connectionservice.config;

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
                .requestMatchers("/connections/health").permitAll()
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

                if (request.getHeader("X-User-Id") != null) {
                    chain.doFilter(request, response);
                    return;
                }

                String authHeader = request.getHeader("Authorization");
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    try {
                        String token = authHeader.substring(7);
                        Key key = Keys.hmacShaKeyFor(secret.getBytes());
                        Claims claims = Jwts.parserBuilder().setSigningKey(key)
                                .build().parseClaimsJws(token).getBody();

                        HttpServletRequestWrapper wrapper = new HttpServletRequestWrapper(request) {
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
package com.alumniconnect.connectionservice.config;

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

    // 🔐 Security Filter Chain
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // ❌ Disable CSRF for Stateless APIs
                .csrf(csrf -> csrf.disable())

                // ❌ Disable default login forms
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())

                // 🔓 Authorization Rules
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/connections/health").permitAll()
                        .anyRequest().authenticated()
                )

                // ❌ No session (Stateless JWT system)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // 🛠️ Add the custom Gateway/JWT Filter
                .addFilterBefore(gatewayJwtFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Filter to bridge the gap between Gateway Headers and Spring Security Context.
     */
    private OncePerRequestFilter gatewayJwtFilter() {
        return new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(HttpServletRequest request,
                                            HttpServletResponse response,
                                            FilterChain chain) throws ServletException, IOException {

                String userId = request.getHeader("X-User-Id");
                String role = request.getHeader("X-User-Role");

                // 1. Handle headers provided by the API Gateway
                if (userId != null) {
                    // Create authentication object to tell Spring this user is LOGGED IN
                    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                            userId,
                            null,
                            Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + (role != null ? role : "USER")))
                    );

                    SecurityContextHolder.getContext().setAuthentication(auth);
                    chain.doFilter(request, response);
                    return;
                }

                // 2. Fallback: Validate JWT directly (Useful for Dev/Testing without Gateway)
                String authHeader = request.getHeader("Authorization");
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    try {
                        String token = authHeader.substring(7);
                        Key key = Keys.hmacShaKeyFor(secret.getBytes());

                        Claims claims = Jwts.parserBuilder()
                                .setSigningKey(key)
                                .build()
                                .parseClaimsJws(token)
                                .getBody();

                        String jwtUserId = String.valueOf(claims.get("userId"));
                        String jwtRole = (String) claims.get("role");

                        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                                jwtUserId,
                                null,
                                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + (jwtRole != null ? jwtRole : "USER")))
                        );

                        SecurityContextHolder.getContext().setAuthentication(auth);
                    } catch (Exception ignored) {
                        // If token is invalid, SecurityContext remains empty, and .anyRequest().authenticated() will block it.
                    }
                }

                chain.doFilter(request, response);
            }
        };
    }
}

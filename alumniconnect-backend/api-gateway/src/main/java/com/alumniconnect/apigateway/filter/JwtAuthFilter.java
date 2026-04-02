package com.alumniconnect.apigateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

@Component
public class JwtAuthFilter extends AbstractGatewayFilterFactory<JwtAuthFilter.Config> {

    @Value("${jwt.secret}")
    private String secret;

    private static final List<String> PUBLIC_PATHS = List.of(
            "/auth/register",
            "/auth/login",
            "/auth/health",
            "/api/auth/register",
            "/api/auth/login"
    );

    public JwtAuthFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {

            ServerHttpRequest request = exchange.getRequest();

            // ✅ 1. Allow CORS preflight requests
            if (request.getMethod() == HttpMethod.OPTIONS) {
                return chain.filter(exchange);
            }

            String path = request.getPath().value();

            // ✅ 2. Allow public endpoints
            boolean isPublic = PUBLIC_PATHS.stream().anyMatch(path::startsWith);
            if (isPublic) {
                return chain.filter(exchange);
            }

            // ✅ 3. Check Authorization header
            if (!request.getHeaders().containsKey("Authorization")) {
                return onError(exchange, "Missing Authorization header", HttpStatus.UNAUTHORIZED);
            }

            String authHeader = request.getHeaders().getFirst("Authorization");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return onError(exchange, "Invalid Authorization header", HttpStatus.UNAUTHORIZED);
            }

            String token = authHeader.substring(7);

            try {
                // ✅ 4. Validate JWT
                SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

                Claims claims = Jwts.parser()
                        .verifyWith(key)   // ✅ NEW METHOD
                        .build()
                        .parseSignedClaims(token)
                        .getPayload();

                if (claims.getExpiration().before(new Date())) {
                    return onError(exchange, "Token expired", HttpStatus.UNAUTHORIZED);
                }

                // ✅ 5. Forward user details
                ServerHttpRequest mutatedRequest = request.mutate()
                        .header("X-User-Id", String.valueOf(claims.get("userId")))
                        .header("X-User-Email", claims.getSubject())
                        .header("X-User-Role", String.valueOf(claims.get("role")))
                        .build();

                return chain.filter(exchange.mutate().request(mutatedRequest).build());

            } catch (JwtException | IllegalArgumentException e) {
                return onError(exchange, "Invalid or expired token", HttpStatus.UNAUTHORIZED);
            }
        };
    }

    private Mono<Void> onError(ServerWebExchange exchange, String message, HttpStatus status) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        return response.setComplete();
    }

    public static class Config { }
}

/*
Step-by-step:
Request hits API Gateway
Filter executes
Check:
    OPTIONS → allow
    Public → allow
Check Authorization header
Extract token
Validate JWT
Check expiry
Add user headers
Forward request*/

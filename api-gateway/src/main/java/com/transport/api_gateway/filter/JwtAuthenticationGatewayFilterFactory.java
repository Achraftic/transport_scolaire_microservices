package com.transport.api_gateway.filter;

import com.transport.api_gateway.util.JwtUtil;
import io.jsonwebtoken.Claims;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class JwtAuthenticationGatewayFilterFactory
        extends AbstractGatewayFilterFactory<JwtAuthenticationGatewayFilterFactory.Config> {

    private final JwtUtil jwtUtil;

    public JwtAuthenticationGatewayFilterFactory(JwtUtil jwtUtil) {
        super(Config.class);
        this.jwtUtil = jwtUtil;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            ServerHttpResponse response = exchange.getResponse();

            // Skip authentication for public endpoints (e.g., /auth/**)
            if (request.getURI().getPath().startsWith("/auth/")) {
                return chain.filter(exchange);
            }

            if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                return this.onError(response, "Missing Authorization header", HttpStatus.UNAUTHORIZED);
            }

            List<String> headers = request.getHeaders().get(HttpHeaders.AUTHORIZATION);
            String authHeader = headers.get(0);

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return this.onError(response, "Invalid Authorization header format", HttpStatus.UNAUTHORIZED);
            }

            String token = authHeader.substring(7);

            if (!jwtUtil.validateToken(token)) {
                return this.onError(response, "JWT Token is invalid or expired", HttpStatus.UNAUTHORIZED);
            }

            try {
                String username = jwtUtil.getClaimFromToken(token, Claims::getSubject);
                String userId = jwtUtil.getClaimFromToken(token, claims -> String.valueOf(claims.get("userId")));

                System.out.println("Claims: " + jwtUtil.getAllClaimsFromToken(token));
                System.out.println("Username from JWT: " + username);
                System.out.println("User ID from JWT: " + userId);
                // Correct: read role as String and convert to List
                String role = jwtUtil.getClaimFromToken(token, claims -> claims.get("role", String.class));
                List<String> roles = role != null ? List.of(role) : List.of();

                ServerHttpRequest modifiedRequest = request.mutate()
                        .header("X-Auth-User", username)
                        .header("X-Auth-Roles", String.join(",", roles))
                        .header("X-Auth-UserId", userId)
                        .build();

                return chain.filter(exchange.mutate().request(modifiedRequest).build());

            } catch (Exception e) {
                System.err.println("Error extracting claims from JWT: " + e.getMessage());
                return this.onError(response, "Error processing JWT claims", HttpStatus.UNAUTHORIZED);
            }
        };
    }

    private Mono<Void> onError(ServerHttpResponse response, String message, HttpStatus httpStatus) {
        response.setStatusCode(httpStatus);
        return response.setComplete();
    }

    public static class Config {
        // Configuration properties can be added here if needed
    }
}

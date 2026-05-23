package com.bank.gateway.security;

import com.bank.gateway.rbac.service.RbacService;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter implements GlobalFilter, Ordered {

    private final JwtUtil jwtUtil;
    private final RbacService rbacService;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, org.springframework.cloud.gateway.filter.GatewayFilterChain chain) {

        String path = exchange.getRequest().getURI().getPath();
        HttpMethod method = exchange.getRequest().getMethod();

        // ✅ PUBLIC ROUTES
        if (isPublic(path)) {
            return chain.filter(exchange);
        }

        String authHeader = exchange.getRequest()
                .getHeaders()
                .getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return unauthorized(exchange);
        }

        String token = authHeader.substring(7);

        try {
            Claims claims = jwtUtil.validateToken(token);

            String role = claims.get("role", String.class);
            String userId = String.valueOf(claims.get("profileId"));

            // ✅ RBAC CHECK
            if (!rbacService.hasAccess(role, path, method)) {
                return forbidden(exchange);
            }

            // ✅ inject headers to microservices
            ServerHttpRequest request = exchange.getRequest()
                    .mutate()
                    .header("X-User-Id", userId)
                    .header("X-Role", role)
                    .build();

            return chain.filter(exchange.mutate().request(request).build());

        } catch (Exception e) {
            return unauthorized(exchange);
        }
    }

    private boolean isPublic(String path) {
        return path.contains("/user/register")
        		|| path.contains("/login")
                || path.contains("/swagger")
                || path.contains("/v3/api-docs")
                || path.startsWith("/uploads");
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }

    private Mono<Void> forbidden(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
        return exchange.getResponse().setComplete();
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
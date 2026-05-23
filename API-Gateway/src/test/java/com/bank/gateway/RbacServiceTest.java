package com.bank.gateway;

import com.bank.gateway.rbac.service.RbacService;
import com.bank.gateway.security.JwtAuthFilter;
import com.bank.gateway.security.JwtUtil;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class JwtAuthFilterTest {

    private JwtUtil jwtUtil;
    private RbacService rbacService;
    private JwtAuthFilter filter;
    private GatewayFilterChain chain;

    @BeforeEach
    void setup() {

        jwtUtil = mock(JwtUtil.class);
        rbacService = mock(RbacService.class);
        chain = mock(GatewayFilterChain.class);

        filter = new JwtAuthFilter(jwtUtil, rbacService);

        when(chain.filter(any()))
                .thenReturn(Mono.empty());
    }

    @Test
    void shouldAllowPublicRoute() {

        MockServerHttpRequest request =
                MockServerHttpRequest.get("/login").build();

        MockServerWebExchange exchange =
                MockServerWebExchange.from(request);

        filter.filter(exchange, chain).block();

        verify(chain, times(1)).filter(any());
    }

    @Test
    void shouldReturnUnauthorizedWhenHeaderMissing() {

        MockServerHttpRequest request =
                MockServerHttpRequest.get("/accounts/admin").build();

        MockServerWebExchange exchange =
                MockServerWebExchange.from(request);

        filter.filter(exchange, chain).block();

        assertEquals(
                401,
                exchange.getResponse().getStatusCode().value()
        );
    }

    @Test
    void shouldReturnUnauthorizedWhenTokenInvalid() {

        MockServerHttpRequest request =
                MockServerHttpRequest.get("/accounts/admin")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer invalid")
                        .build();

        MockServerWebExchange exchange =
                MockServerWebExchange.from(request);

        when(jwtUtil.validateToken(any()))
                .thenThrow(new RuntimeException());

        filter.filter(exchange, chain).block();

        assertEquals(
                401,
                exchange.getResponse().getStatusCode().value()
        );
    }

    @Test
    void shouldReturnForbiddenWhenRoleDenied() {

        Claims claims = mock(Claims.class);

        when(claims.get("role", String.class))
                .thenReturn("USER");

        when(claims.get("profileId"))
                .thenReturn(5);

        when(jwtUtil.validateToken(any()))
                .thenReturn(claims);

        when(rbacService.hasAccess(
                any(),
                any(),
                any(HttpMethod.class)
        )).thenReturn(false);

        MockServerHttpRequest request =
                MockServerHttpRequest.get("/accounts/admin")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer token")
                        .build();

        MockServerWebExchange exchange =
                MockServerWebExchange.from(request);

        filter.filter(exchange, chain).block();

        assertEquals(
                403,
                exchange.getResponse().getStatusCode().value()
        );
    }

    @Test
    void shouldAllowAuthorizedRequest() {

        Claims claims = mock(Claims.class);

        when(claims.get("role", String.class))
                .thenReturn("ADMIN");

        when(claims.get("profileId"))
                .thenReturn(1);

        when(jwtUtil.validateToken(any()))
                .thenReturn(claims);

        when(rbacService.hasAccess(
                any(),
                any(),
                any(HttpMethod.class)
        )).thenReturn(true);

        MockServerHttpRequest request =
                MockServerHttpRequest.get("/accounts/admin")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer token")
                        .build();

        MockServerWebExchange exchange =
                MockServerWebExchange.from(request);

        filter.filter(exchange, chain).block();

        verify(chain, times(1)).filter(any());
    }
}
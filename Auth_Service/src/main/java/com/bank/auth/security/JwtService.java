package com.bank.auth.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    private Key key() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String generateToken(Integer profileId, String customerId, String role) {

        return Jwts.builder()
                .setSubject(customerId)
                .claim("profileId", profileId)
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() +86400000))
                .signWith(key(), SignatureAlgorithm.HS256)
                .compact();
    }

    public Claims extract(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
package com.ecommerce.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * JwtUtil handles JWT token generation and validation.
 * Why this class exists: Centralized place to manage JWT logic, avoiding scattered auth code.
 * Single Responsibility: Create, validate, and extract claims from JWT tokens.
 * 
 * Interview talking point: 
 * - We use JWT for stateless authentication (no session storage needed)
 * - Token contains user email, can be validated without database lookup
 * - Signature ensures token hasn't been tampered with
 */
@Component
public class JwtUtil {
    
    @Value("${jwt.secret}")
    private String jwtSecret;
    
    @Value("${jwt.expiration}")
    private long jwtExpirationMs;
    
    // Generate JWT token for a user
    public String generateToken(String email) {
        return Jwts.builder()
                .subject(email) // Subject is the user's email
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }
    
    // Extract email from token
    public String extractEmail(String token) {
        return getClaims(token).getSubject();
    }
    
    // Check if token is valid (not expired and signature is correct)
    public boolean isTokenValid(String token) {
        try {
            getClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    // Helper method: extract claims from token
    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
    
    // Helper method: get signing key from secret
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }
}

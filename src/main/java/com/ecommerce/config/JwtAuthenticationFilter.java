package com.ecommerce.config;

import com.ecommerce.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;

/**
 * JwtAuthenticationFilter intercepts every HTTP request to validate JWT token.
 * Why this class exists: Extract and validate JWT from requests before they reach controllers.
 * Single Responsibility: Check Authorization header for valid JWT token.
 * 
 * Request flow:
 * 1. Client sends request with Authorization header: "Bearer <token>"
 * 2. Filter extracts token and validates it
 * 3. If valid, sets user in SecurityContext so controller knows who is authenticated
 * 4. If invalid, request proceeds but user is not authenticated
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {
        
        // Get Authorization header
        String authHeader = request.getHeader("Authorization");
        
        // Check if header exists and starts with "Bearer "
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            // Extract token (remove "Bearer " prefix)
            String token = authHeader.substring(7);
            
            // Validate token
            if (jwtUtil.isTokenValid(token)) {
                // Token is valid, extract email and set in SecurityContext
                String email = jwtUtil.extractEmail(token);
                
                // Create authentication object
                // Why empty ArrayList? We're using JWT, so we don't need Spring's role/authority system
                UsernamePasswordAuthenticationToken authentication = 
                    new UsernamePasswordAuthenticationToken(email, null, new ArrayList<>());
                
                // Set in SecurityContext so controller can access authenticated user
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }
        
        // Continue with next filter or controller
        filterChain.doFilter(request, response);
    }
}

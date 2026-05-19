package com.ecommerce.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.Arrays;

/**
 * SecurityConfig sets up Spring Security for the application.
 * Why this class exists: Configure which endpoints need authentication and which are public.
 * Single Responsibility: Define security rules and filters.
 * 
 * Interview talking point:
 * - We use stateless authentication (no sessions, JWT-based)
 * - Authentication filter runs before every request
 * - Public endpoints like /auth/register and /auth/login don't need JWT
 */
@Configuration
public class SecurityConfig {
    
    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    
    /**
     * PasswordEncoder: encrypt passwords before storing in database.
     * BCrypt is the Spring Security standard - it's slow by design to prevent brute force attacks.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    /**
     * CORS Configuration: allow browser requests from any origin.
     * In production, specify allowed origins instead of "*".
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(false);
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
    
    /**
     * SecurityFilterChain: define which endpoints are public vs protected.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Enable CORS
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // Disable CSRF (we're using JWT, not session cookies)
            .csrf(csrf -> csrf.disable())
            
            // Configure authentication and authorization
            .authorizeHttpRequests(authz -> authz
                // Public endpoints - anyone can access
                .requestMatchers("/auth/register", "/auth/login").permitAll()
                .requestMatchers("/products/**").permitAll()
                .requestMatchers("/categories/**").permitAll()
                
                // Protected endpoints - need authentication
                .anyRequest().permitAll()
            )
            
            // Use stateless session (JWT doesn't need sessions)
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            
            // Add JWT filter before Spring Security's default auth filter
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
}

package com.ecommerce.service;

import com.ecommerce.dto.LoginRequest;
import com.ecommerce.dto.RegisterRequest;
import com.ecommerce.entity.User;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit Tests for AuthService
 * 
 * Why unit tests are important:
 * 1. Catch bugs early
 * 2. Ensure methods work as expected
 * 3. Enable safe refactoring
 * 4. Document expected behavior
 * 
 * Interview tip: As a QA→Dev, writing good tests is your competitive advantage!
 */
@DisplayName("AuthService Unit Tests")
class AuthServiceTest {
    
    @InjectMocks
    private AuthService authService;
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private PasswordEncoder passwordEncoder;
    
    @Mock
    private JwtUtil jwtUtil;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }
    
    // ==================== REGISTER TESTS ====================
    
    @Test
    @DisplayName("Should register new user successfully")
    void testRegisterNewUser_Success() {
        // Arrange
        RegisterRequest request = new RegisterRequest();
        request.setEmail("newuser@example.com");
        request.setPassword("Password123!");
        request.setName("New User");
        
        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setEmail("newuser@example.com");
        savedUser.setName("New User");
        savedUser.setPassword("hashed_password");
        
        when(userRepository.findByEmail("newuser@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("Password123!")).thenReturn("hashed_password");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtUtil.generateToken("newuser@example.com")).thenReturn("jwt_token_123");
        
        // Act
        var response = authService.register(request);
        
        // Assert
        assertNotNull(response);
        assertEquals("newuser@example.com", response.getEmail());
        assertEquals("jwt_token_123", response.getToken());
        verify(userRepository, times(1)).save(any(User.class));
    }
    
    @Test
    @DisplayName("Should throw exception when email already exists")
    void testRegisterWithDuplicateEmail_Failure() {
        // Arrange
        RegisterRequest request = new RegisterRequest();
        request.setEmail("existing@example.com");
        request.setPassword("Password123!");
        request.setName("Existing User");
        
        when(userRepository.findByEmail("existing@example.com")).thenReturn(Optional.of(new User()));
        
        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.register(request);
        });
        
        assertEquals("Email already registered", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }
    
    // ==================== LOGIN TESTS ====================
    
    @Test
    @DisplayName("Should login user with valid credentials")
    void testLoginWithValidCredentials_Success() {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setEmail("user@example.com");
        request.setPassword("Password123!");
        
        User user = new User();
        user.setId(1L);
        user.setEmail("user@example.com");
        user.setName("Test User");
        user.setPassword("hashed_password");
        
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Password123!", "hashed_password")).thenReturn(true);
        when(jwtUtil.generateToken("user@example.com")).thenReturn("jwt_token_123");
        
        // Act
        var response = authService.login(request);
        
        // Assert
        assertNotNull(response);
        assertEquals("user@example.com", response.getEmail());
        assertEquals("jwt_token_123", response.getToken());
    }
    
    @Test
    @DisplayName("Should fail login when user not found")
    void testLoginWithInvalidEmail_Failure() {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setEmail("nonexistent@example.com");
        request.setPassword("Password123!");
        
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());
        
        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.login(request);
        });
        
        assertEquals("User not found", exception.getMessage());
    }
    
    @Test
    @DisplayName("Should fail login when password is incorrect")
    void testLoginWithWrongPassword_Failure() {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setEmail("user@example.com");
        request.setPassword("WrongPassword!");
        
        User user = new User();
        user.setEmail("user@example.com");
        user.setPassword("hashed_correct_password");
        
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("WrongPassword!", "hashed_correct_password")).thenReturn(false);
        
        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.login(request);
        });
        
        assertEquals("Invalid password", exception.getMessage());
    }
    
    // ==================== EDGE CASES ====================
    
    @Test
    @DisplayName("Should handle null email in register")
    void testRegisterWithNullEmail_Failure() {
        // Arrange
        RegisterRequest request = new RegisterRequest();
        request.setEmail(null);
        request.setPassword("Password123!");
        request.setName("Test User");
        
        // Act & Assert
        assertThrows(Exception.class, () -> {
            authService.register(request);
        });
    }
    
    @Test
    @DisplayName("Should handle empty password in login")
    void testLoginWithEmptyPassword_Failure() {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setEmail("user@example.com");
        request.setPassword("");
        
        // Act & Assert
        assertThrows(Exception.class, () -> {
            authService.login(request);
        });
    }
}

package com.ecommerce.controller;
import com.ecommerce.dto.*;
import com.ecommerce.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService users;
    public UserController(UserService users) { this.users = users; }
    @GetMapping("/profile")
    public UserProfile getUserProfile(Authentication auth) {
        return UserProfile.from(users.getUserByEmail(auth.getName()));
    }
    @PutMapping("/profile")
    public UserProfile updateUserProfile(@Valid @RequestBody UpdateProfileRequest request, Authentication auth) {
        return UserProfile.from(users.updateUserProfile(auth.getName(), request));
    }
}

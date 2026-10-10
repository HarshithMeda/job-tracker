package com.harshith.job_tracker.controller;

import com.harshith.job_tracker.dto.AuthResponse;
import com.harshith.job_tracker.dto.LoginRequest;
import com.harshith.job_tracker.dto.RegisterRequest;
import com.harshith.job_tracker.dto.UserResponse;
import com.harshith.job_tracker.model.AppUser;
import com.harshith.job_tracker.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(@Valid @RequestBody RegisterRequest request) {
        AppUser user = authService.register(request);
        return new UserResponse(user.getId(), user.getEmail());
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }
}

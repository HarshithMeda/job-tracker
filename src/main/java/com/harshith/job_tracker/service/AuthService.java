package com.harshith.job_tracker.service;

import com.harshith.job_tracker.dto.AuthResponse;
import com.harshith.job_tracker.dto.LoginRequest;
import com.harshith.job_tracker.dto.RegisterRequest;
import com.harshith.job_tracker.exception.DuplicateResourceException;
import com.harshith.job_tracker.exception.InvalidCredentialsException;
import com.harshith.job_tracker.model.AppUser;
import com.harshith.job_tracker.repository.UserRepository;
import com.harshith.job_tracker.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public AppUser register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("Email is already registered");
        }
        AppUser user = new AppUser();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        return userRepository.save(user);
    }

    public AuthResponse login(LoginRequest request) {
        String email = request.email().trim().toLowerCase();
        AppUser user = userRepository.findByEmail(email)
                .orElseThrow(InvalidCredentialsException::new);
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }
        return new AuthResponse(jwtService.generateToken(user), "Bearer", jwtService.getExpirationSeconds());
    }
}

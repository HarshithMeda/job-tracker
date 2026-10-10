package com.harshith.job_tracker.dto;

public record AuthResponse(String accessToken, String tokenType, long expiresInSeconds) {
}

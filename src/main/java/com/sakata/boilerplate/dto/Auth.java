package com.sakata.boilerplate.dto;

import jakarta.validation.constraints.*;

public class Auth {
    public record LoginRequest(@NotBlank String username, @NotBlank String password) {
    }

    public record RegisterRequest(
            @NotBlank @Size(min = 3, max = 50) String username,
            @NotBlank @Email String email,
            @NotBlank @Size(min = 6) String password) {
    }

    public record TokenResponse(String accessToken, String tokenType, long expiresIn) {
        public static TokenResponse of(String t, long ms) {
            return new TokenResponse(t, "Bearer", ms / 1000);
        }
    }
}

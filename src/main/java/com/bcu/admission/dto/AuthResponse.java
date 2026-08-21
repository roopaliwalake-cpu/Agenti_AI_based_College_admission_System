package com.bcu.admission.dto;

public record AuthResponse(
        String role,
        String email,
        String message
) {
}

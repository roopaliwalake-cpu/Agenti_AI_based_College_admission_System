package com.bcu.admission.dto;

import java.time.LocalDateTime;

public record MeritStatusResponse(
        boolean found,
        boolean selected,
        Integer rankPosition,
        String studentName,
        String courseName,
        String department,
        Double percentage,
        String message,
        LocalDateTime releasedAt
) {
}

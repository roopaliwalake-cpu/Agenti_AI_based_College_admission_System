package com.bcu.admission.dto;

public record CourseRecommendation(
        String courseName,
        String duration,
        String eligibility,
        String reason,
        String source
) {
}

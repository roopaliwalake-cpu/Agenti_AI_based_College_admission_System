package com.bcu.admission.dto;

import java.math.BigDecimal;

public record CollegeSearchRequest(
        String courseName,
        String collegeType,
        String location,
        BigDecimal maximumBudget
) {
}

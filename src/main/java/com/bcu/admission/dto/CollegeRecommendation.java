package com.bcu.admission.dto;

import java.math.BigDecimal;

public record CollegeRecommendation(
        String collegeName,
        String collegeType,
        String location,
        String courseAvailability,
        BigDecimal estimatedFees,
        String duration,
        String eligibility,
        String hostelAvailability,
        String placementInformation,
        String officialSource
) {
}

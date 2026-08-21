package com.bcu.admission.dto;

import java.util.List;

public record AgentResponse(
        String summary,
        String extractedDegree,
        Double extractedPercentage,
        String interestArea,
        List<CourseRecommendation> recommendations,
        String nextQuestion,
        boolean aiEnhanced
) {
}

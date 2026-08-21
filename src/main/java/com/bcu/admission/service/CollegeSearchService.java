package com.bcu.admission.service;

import com.bcu.admission.dto.CollegeRecommendation;
import com.bcu.admission.dto.CollegeSearchRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class CollegeSearchService {
    public List<CollegeRecommendation> search(CollegeSearchRequest request) {
        return List.of(
                new CollegeRecommendation(
                        "Bangalore University",
                        "University",
                        "Jnanabharathi Campus, Bengaluru",
                        availability(request.courseName()),
                        new BigDecimal("25000"),
                        "2 years",
                        "Verify latest eligibility, seat matrix, and fee notification from Bangalore University.",
                        "Check official university notice",
                        "Placement and department information should be verified with the university office.",
                        "https://bangaloreuniversity.karnataka.gov.in/"
                )
        );
    }

    private String availability(String courseName) {
        if (courseName == null || courseName.isBlank()) {
            return "Course availability must be confirmed from official notification.";
        }
        return courseName + " at Bangalore University must be confirmed from the official admission notification.";
    }
}

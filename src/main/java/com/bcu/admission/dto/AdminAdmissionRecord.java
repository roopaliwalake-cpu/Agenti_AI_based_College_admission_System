package com.bcu.admission.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AdminAdmissionRecord(
        Long admissionId,
        Long studentId,
        String studentName,
        String email,
        String phone,
        String degree,
        Double percentage,
        String department,
        String courseName,
        String collegeName,
        String collegeType,
        BigDecimal fees,
        LocalDateTime admissionDate,
        String admissionStatus
) {
}

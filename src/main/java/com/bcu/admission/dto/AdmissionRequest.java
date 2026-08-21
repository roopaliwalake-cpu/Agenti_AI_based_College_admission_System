package com.bcu.admission.dto;

import com.bcu.admission.model.Student;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

public record AdmissionRequest(
        @Valid Student student,
        @NotBlank String courseName,
        @NotBlank String collegeName,
        @NotBlank String collegeType,
        BigDecimal fees,
        String duration,
        String collegeSource,
        boolean policyAccepted
) {
}

package com.bcu.admission.repository;

import com.bcu.admission.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByAdmissionAdmissionId(Long admissionId);
}

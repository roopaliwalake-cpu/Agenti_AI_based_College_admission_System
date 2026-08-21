package com.bcu.admission.repository;

import com.bcu.admission.model.PolicyAcceptance;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PolicyAcceptanceRepository extends JpaRepository<PolicyAcceptance, Long> {
}

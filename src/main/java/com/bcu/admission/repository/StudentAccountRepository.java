package com.bcu.admission.repository;

import com.bcu.admission.model.StudentAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StudentAccountRepository extends JpaRepository<StudentAccount, Long> {
    Optional<StudentAccount> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);
}

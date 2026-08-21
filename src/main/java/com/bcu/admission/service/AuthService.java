package com.bcu.admission.service;

import com.bcu.admission.dto.AuthRequest;
import com.bcu.admission.dto.AuthResponse;
import com.bcu.admission.model.StudentAccount;
import com.bcu.admission.repository.StudentAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Locale;

@Service
public class AuthService {
    private static final String ADMIN_EMAIL = "babhitha@gmail.com";
    private static final String ADMIN_PASSWORD = "babhitha@123";

    private final StudentAccountRepository studentAccountRepository;

    public AuthService(StudentAccountRepository studentAccountRepository) {
        this.studentAccountRepository = studentAccountRepository;
    }

    @Transactional
    public AuthResponse registerStudent(AuthRequest request) {
        String email = normalizeEmail(request.email());
        validatePassword(request.password());
        if (studentAccountRepository.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException("A student account already exists for this email.");
        }

        StudentAccount account = new StudentAccount();
        account.setEmail(email);
        account.setPasswordHash(hashPassword(email, request.password()));
        account.setCreatedAt(LocalDateTime.now());
        studentAccountRepository.save(account);
        return new AuthResponse("STUDENT", email, "Registration successful. You can now continue to the student portal.");
    }

    public AuthResponse loginStudent(AuthRequest request) {
        String email = normalizeEmail(request.email());
        StudentAccount account = studentAccountRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new IllegalArgumentException("Student account not found. Please register first."));
        if (!account.getPasswordHash().equals(hashPassword(email, request.password()))) {
            throw new IllegalArgumentException("Invalid student email or password.");
        }
        return new AuthResponse("STUDENT", account.getEmail(), "Student login successful.");
    }

    public AuthResponse loginAdmin(AuthRequest request) {
        String email = normalizeEmail(request.email());
        if (!ADMIN_EMAIL.equalsIgnoreCase(email) || !ADMIN_PASSWORD.equals(request.password())) {
            throw new IllegalArgumentException("Invalid admin email or password.");
        }
        return new AuthResponse("ADMIN", ADMIN_EMAIL, "Admin login successful.");
    }

    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email is required.");
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private void validatePassword(String password) {
        if (password == null || password.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters.");
        }
    }

    private String hashPassword(String email, String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest((email + ":" + password).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("Password hashing is not available.", exception);
        }
    }
}

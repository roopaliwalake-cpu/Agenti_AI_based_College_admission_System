package com.bcu.admission.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "policy_acceptances")
public class PolicyAcceptance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long acceptanceId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "student_id")
    private Student student;

    private String policyId;
    private LocalDateTime acceptedDate;
    private String status;

    public Long getAcceptanceId() {
        return acceptanceId;
    }

    public void setAcceptanceId(Long acceptanceId) {
        this.acceptanceId = acceptanceId;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public String getPolicyId() {
        return policyId;
    }

    public void setPolicyId(String policyId) {
        this.policyId = policyId;
    }

    public LocalDateTime getAcceptedDate() {
        return acceptedDate;
    }

    public void setAcceptedDate(LocalDateTime acceptedDate) {
        this.acceptedDate = acceptedDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

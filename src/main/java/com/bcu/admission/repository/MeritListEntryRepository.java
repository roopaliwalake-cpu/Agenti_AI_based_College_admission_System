package com.bcu.admission.repository;

import com.bcu.admission.model.MeritListEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MeritListEntryRepository extends JpaRepository<MeritListEntry, Long> {
    void deleteByCourseNameIgnoreCase(String courseName);

    List<MeritListEntry> findByCourseNameIgnoreCaseOrderByRankPositionAsc(String courseName);

    Optional<MeritListEntry> findFirstByAdmission_AdmissionIdOrderByReleasedAtDesc(Long admissionId);

    Optional<MeritListEntry> findFirstByAdmission_Student_EmailIgnoreCaseOrderByReleasedAtDesc(String email);
}

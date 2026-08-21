package com.bcu.admission.repository;

import com.bcu.admission.model.Admission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AdmissionRepository extends JpaRepository<Admission, Long> {
    List<Admission> findByStudent_NameContainingIgnoreCaseOrStudent_EmailContainingIgnoreCaseOrCourseNameContainingIgnoreCaseOrCollegeNameContainingIgnoreCase(
            String studentName,
            String studentEmail,
            String courseName,
            String collegeName
    );
}

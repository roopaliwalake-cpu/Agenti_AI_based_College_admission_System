package com.bcu.admission.service;

import com.bcu.admission.dto.AdmissionRequest;
import com.bcu.admission.dto.AdminAdmissionRecord;
import com.bcu.admission.dto.ConfirmationResponse;
import com.bcu.admission.dto.MeritStatusResponse;
import com.bcu.admission.dto.PaymentRequest;
import com.bcu.admission.model.Admission;
import com.bcu.admission.model.MeritListEntry;
import com.bcu.admission.model.Payment;
import com.bcu.admission.model.PolicyAcceptance;
import com.bcu.admission.model.Student;
import com.bcu.admission.repository.AdmissionRepository;
import com.bcu.admission.repository.MeritListEntryRepository;
import com.bcu.admission.repository.PaymentRepository;
import com.bcu.admission.repository.PolicyAcceptanceRepository;
import com.bcu.admission.repository.StudentRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class AdmissionService {
    private final AdmissionRepository admissionRepository;
    private final StudentRepository studentRepository;
    private final PaymentRepository paymentRepository;
    private final PolicyAcceptanceRepository policyAcceptanceRepository;
    private final MeritListEntryRepository meritListEntryRepository;

    public AdmissionService(
            AdmissionRepository admissionRepository,
            StudentRepository studentRepository,
            PaymentRepository paymentRepository,
            PolicyAcceptanceRepository policyAcceptanceRepository,
            MeritListEntryRepository meritListEntryRepository
    ) {
        this.admissionRepository = admissionRepository;
        this.studentRepository = studentRepository;
        this.paymentRepository = paymentRepository;
        this.policyAcceptanceRepository = policyAcceptanceRepository;
        this.meritListEntryRepository = meritListEntryRepository;
    }

    @Transactional
    public Admission createAdmission(AdmissionRequest request) {
        if (!request.policyAccepted()) {
            throw new IllegalArgumentException("Admission policy must be accepted before submission.");
        }

        Student student = studentRepository.findByEmail(request.student().getEmail())
                .map(existing -> updateStudent(existing, request.student()))
                .orElse(request.student());

        Admission admission = new Admission();
        admission.setStudent(student);
        admission.setCourseName(request.courseName());
        admission.setCollegeName(request.collegeName());
        admission.setCollegeType(request.collegeType());
        admission.setFees(request.fees());
        admission.setDuration(request.duration());
        admission.setCollegeSource(request.collegeSource());
        admission.setAdmissionDate(LocalDateTime.now());
        admission.setStatus("FORM_SUBMITTED_PAYMENT_PENDING");

        Admission saved = admissionRepository.save(admission);
        PolicyAcceptance acceptance = new PolicyAcceptance();
        acceptance.setStudent(saved.getStudent());
        acceptance.setPolicyId("BCU_ADMISSION_POLICY");
        acceptance.setAcceptedDate(LocalDateTime.now());
        acceptance.setStatus("ACCEPTED");
        policyAcceptanceRepository.save(acceptance);
        return saved;
    }

    @Transactional
    public Payment simulatePayment(PaymentRequest request) {
        Admission admission = admissionRepository.findById(request.admissionId())
                .orElseThrow(() -> new EntityNotFoundException("Admission not found"));

        Payment payment = new Payment();
        payment.setAdmission(admission);
        payment.setAmount(request.amount());
        payment.setTransactionId(request.transactionReference() == null || request.transactionReference().isBlank()
                ? "SIM-" + UUID.randomUUID()
                : request.transactionReference());
        payment.setPaymentDate(LocalDateTime.now());
        payment.setPaymentStatus("SUCCESS");

        admission.setStatus("CONFIRMED");
        admissionRepository.save(admission);
        return paymentRepository.save(payment);
    }

    public ConfirmationResponse confirmation(Long admissionId) {
        Admission admission = admissionRepository.findById(admissionId)
                .orElseThrow(() -> new EntityNotFoundException("Admission not found"));
        Payment payment = paymentRepository.findByAdmissionAdmissionId(admissionId).orElse(null);
        return new ConfirmationResponse(
                admission.getAdmissionId(),
                payment == null ? null : payment.getPaymentId(),
                admission.getStudent().getName(),
                admission.getStudent().getEmail(),
                admission.getCourseName(),
                admission.getCollegeName(),
                admission.getAdmissionDate(),
                payment == null ? null : payment.getAmount(),
                payment == null ? "PENDING" : payment.getPaymentStatus(),
                admission.getStatus()
        );
    }

    public String feeReceipt(Long admissionId) {
        ConfirmationResponse confirmation = confirmation(admissionId);
        if (!"SUCCESS".equalsIgnoreCase(confirmation.paymentStatus())) {
            throw new IllegalArgumentException("Fee receipt is available only after successful payment.");
        }

        return """
                BANGALORE UNIVERSITY
                FEE PAYMENT RECEIPT

                Admission ID: %s
                Payment ID: %s
                Student Name: %s
                Email: %s
                Course: %s
                University: %s
                Paid Amount: Rs. %s
                Payment Status: %s
                Admission Status: %s
                Admission Date: %s

                This is a system-generated receipt for the simulated project payment.
                """.formatted(
                confirmation.admissionId(),
                confirmation.paymentId(),
                confirmation.studentName(),
                confirmation.email(),
                confirmation.courseName(),
                confirmation.collegeName(),
                confirmation.paidAmount(),
                confirmation.paymentStatus(),
                confirmation.admissionStatus(),
                confirmation.admissionDate()
        );
    }

    public List<Admission> searchAdmissions(String query) {
        if (query == null || query.isBlank()) {
            return admissionRepository.findAll();
        }
        return admissionRepository.findByStudent_NameContainingIgnoreCaseOrStudent_EmailContainingIgnoreCaseOrCourseNameContainingIgnoreCaseOrCollegeNameContainingIgnoreCase(
                query, query, query, query
        );
    }

    public List<AdminAdmissionRecord> adminRecords(String query, String department) {
        String departmentFilter = department == null ? "" : department.trim();
        return searchAdmissions(query).stream()
                .map(this::toAdminRecord)
                .filter(record -> departmentFilter.isBlank()
                        || "All Departments".equalsIgnoreCase(departmentFilter)
                        || record.department().equalsIgnoreCase(departmentFilter))
                .sorted(Comparator.comparing(
                                AdminAdmissionRecord::percentage,
                                Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(AdminAdmissionRecord::studentName, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    public String adminRecordsCsv(String query, String department) {
        StringBuilder csv = new StringBuilder();
        csv.append("Admission ID,Student ID,Student Name,Email,Phone,Degree,Percentage,Department,Course,University,Institution Type,Fees,Admission Date,Admission Status\n");
        adminRecords(query, department).forEach(record -> csv
                .append(csvValue(record.admissionId())).append(',')
                .append(csvValue(record.studentId())).append(',')
                .append(csvValue(record.studentName())).append(',')
                .append(csvValue(record.email())).append(',')
                .append(csvValue(record.phone())).append(',')
                .append(csvValue(record.degree())).append(',')
                .append(csvValue(record.percentage())).append(',')
                .append(csvValue(record.department())).append(',')
                .append(csvValue(record.courseName())).append(',')
                .append(csvValue(record.collegeName())).append(',')
                .append(csvValue(record.collegeType())).append(',')
                .append(csvValue(record.fees())).append(',')
                .append(csvValue(record.admissionDate())).append(',')
                .append(csvValue(record.admissionStatus())).append('\n'));
        return csv.toString();
    }

    @Transactional
    public List<AdminAdmissionRecord> releaseMeritList(String courseName) {
        if (courseName == null || courseName.isBlank()) {
            throw new IllegalArgumentException("Please select a course before releasing the merit list.");
        }

        meritListEntryRepository.deleteByCourseNameIgnoreCase(courseName.trim());
        List<Admission> selectedAdmissions = searchAdmissions(courseName).stream()
                .filter(record -> record.getCourseName().equalsIgnoreCase(courseName.trim()))
                .sorted(Comparator.comparing(
                                (Admission admission) -> admission.getStudent().getPercentage(),
                                Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing((Admission admission) -> admission.getStudent().getName(), String.CASE_INSENSITIVE_ORDER))
                .limit(5)
                .toList();

        for (int index = 0; index < selectedAdmissions.size(); index++) {
            MeritListEntry entry = new MeritListEntry();
            entry.setAdmission(selectedAdmissions.get(index));
            entry.setCourseName(courseName.trim());
            entry.setRankPosition(index + 1);
            entry.setSelected(true);
            entry.setReleasedAt(LocalDateTime.now());
            meritListEntryRepository.save(entry);
            selectedAdmissions.get(index).setStatus("MERIT_SELECTED");
            admissionRepository.save(selectedAdmissions.get(index));
        }

        return selectedAdmissions.stream().map(this::toAdminRecord).toList();
    }

    public MeritStatusResponse meritStatus(String email, Long admissionId) {
        if ((email == null || email.isBlank()) && admissionId == null) {
            throw new IllegalArgumentException("Enter email or admission ID to check merit status.");
        }

        return (admissionId != null
                ? meritListEntryRepository.findFirstByAdmission_AdmissionIdOrderByReleasedAtDesc(admissionId)
                : meritListEntryRepository.findFirstByAdmission_Student_EmailIgnoreCaseOrderByReleasedAtDesc(email.trim()))
                .map(this::toMeritStatus)
                .orElse(new MeritStatusResponse(
                        false,
                        false,
                        null,
                        null,
                        null,
                        null,
                        null,
                        "Merit list is not released for this student yet, or the student was not selected in the top 5.",
                        null
                ));
    }

    private MeritStatusResponse toMeritStatus(MeritListEntry entry) {
        Admission admission = entry.getAdmission();
        Student student = admission.getStudent();
        return new MeritStatusResponse(
                true,
                Boolean.TRUE.equals(entry.getSelected()),
                entry.getRankPosition(),
                student.getName(),
                admission.getCourseName(),
                departmentFor(admission.getCourseName()),
                student.getPercentage(),
                "Congratulations! You are selected in the released merit list.",
                entry.getReleasedAt()
        );
    }

    private AdminAdmissionRecord toAdminRecord(Admission admission) {
        Student student = admission.getStudent();
        return new AdminAdmissionRecord(
                admission.getAdmissionId(),
                student.getStudentId(),
                student.getName(),
                student.getEmail(),
                student.getPhone(),
                student.getDegree(),
                student.getPercentage(),
                departmentFor(admission.getCourseName()),
                admission.getCourseName(),
                admission.getCollegeName(),
                admission.getCollegeType(),
                admission.getFees(),
                admission.getAdmissionDate(),
                admission.getStatus()
        );
    }

    private String departmentFor(String courseName) {
        String course = courseName == null ? "" : courseName.toLowerCase(Locale.ROOT);
        if (course.contains("mca") || course.contains("computer") || course.contains("data")) {
            return "Computer Science";
        }
        if (course.contains("zoology") || course.contains("botany")) {
            return "Life Sciences";
        }
        if (course.contains("m.com") || course.contains("commerce") || course.contains("account")
                || course.contains("finance") || course.contains("banking") || course.contains("insurance")) {
            return "Commerce";
        }
        if (course.contains("m.a") || course.contains("political") || course.contains("economics")
                || course.contains("history") || course.contains("social")) {
            return "Arts and Humanities";
        }
        return "General PG";
    }

    private String csvValue(Object value) {
        if (value == null) {
            return "";
        }
        String text = value.toString().replace("\"", "\"\"");
        return "\"" + text + "\"";
    }

    private Student updateStudent(Student existing, Student incoming) {
        existing.setName(incoming.getName());
        existing.setPhone(incoming.getPhone());
        existing.setDateOfBirth(incoming.getDateOfBirth());
        existing.setAddress(incoming.getAddress());
        existing.setDegree(incoming.getDegree());
        existing.setUniversity(incoming.getUniversity());
        existing.setPercentage(incoming.getPercentage());
        return existing;
    }
}

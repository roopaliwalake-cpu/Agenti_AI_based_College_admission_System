package com.bcu.admission.controller;

import com.bcu.admission.dto.*;
import com.bcu.admission.model.Admission;
import com.bcu.admission.model.Payment;
import com.bcu.admission.service.AdmissionAgentService;
import com.bcu.admission.service.AdmissionService;
import com.bcu.admission.service.AuthService;
import com.bcu.admission.service.CollegeSearchService;
import com.bcu.admission.service.CourseCatalogService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class AdmissionApiController {
    private final AdmissionAgentService admissionAgentService;
    private final CourseCatalogService courseCatalogService;
    private final CollegeSearchService collegeSearchService;
    private final AdmissionService admissionService;
    private final AuthService authService;

    public AdmissionApiController(
            AdmissionAgentService admissionAgentService,
            CourseCatalogService courseCatalogService,
            CollegeSearchService collegeSearchService,
            AdmissionService admissionService,
            AuthService authService
    ) {
        this.admissionAgentService = admissionAgentService;
        this.courseCatalogService = courseCatalogService;
        this.collegeSearchService = collegeSearchService;
        this.admissionService = admissionService;
        this.authService = authService;
    }

    @PostMapping("/auth/student/register")
    public AuthResponse registerStudent(@Valid @RequestBody AuthRequest request) {
        return authService.registerStudent(request);
    }

    @PostMapping("/auth/student/login")
    public AuthResponse loginStudent(@Valid @RequestBody AuthRequest request) {
        return authService.loginStudent(request);
    }

    @PostMapping("/auth/admin/login")
    public AuthResponse loginAdmin(@Valid @RequestBody AuthRequest request) {
        return authService.loginAdmin(request);
    }

    @PostMapping("/agent/analyze")
    public AgentResponse analyze(@Valid @RequestBody AgentRequest request) {
        return admissionAgentService.analyze(request.message());
    }

    @GetMapping("/courses")
    public List<CourseRecommendation> courses(
            @RequestParam(defaultValue = "") String degree,
            @RequestParam(required = false) Double percentage,
            @RequestParam(defaultValue = "") String interest
    ) {
        return courseCatalogService.recommend(degree, percentage, interest);
    }

    @PostMapping("/colleges/search")
    public List<CollegeRecommendation> searchColleges(@RequestBody CollegeSearchRequest request) {
        return collegeSearchService.search(request);
    }

    @PostMapping("/admissions")
    public Admission createAdmission(@Valid @RequestBody AdmissionRequest request) {
        return admissionService.createAdmission(request);
    }

    @PostMapping("/payments/simulate")
    public Payment simulatePayment(@Valid @RequestBody PaymentRequest request) {
        return admissionService.simulatePayment(request);
    }

    @GetMapping("/confirmations/{admissionId}")
    public ConfirmationResponse confirmation(@PathVariable Long admissionId) {
        return admissionService.confirmation(admissionId);
    }

    @GetMapping("/confirmations/{admissionId}/receipt")
    public ResponseEntity<String> downloadFeeReceipt(@PathVariable Long admissionId) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=fee-receipt-" + admissionId + ".txt")
                .contentType(MediaType.TEXT_PLAIN)
                .body(admissionService.feeReceipt(admissionId));
    }

    @GetMapping("/admin/admissions")
    public List<AdminAdmissionRecord> adminAdmissions(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String department
    ) {
        return admissionService.adminRecords(query, department);
    }

    @GetMapping("/admin/admissions/download")
    public ResponseEntity<String> downloadAdminAdmissions(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String department
    ) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=bcu-admission-records.csv")
                .contentType(new MediaType("text", "csv"))
                .body(admissionService.adminRecordsCsv(query, department));
    }

    @PostMapping("/admin/merit-list/release")
    public List<AdminAdmissionRecord> releaseMeritList(@RequestParam String courseName) {
        return admissionService.releaseMeritList(courseName);
    }

    @GetMapping("/students/merit-status")
    public MeritStatusResponse meritStatus(
            @RequestParam(required = false) String email,
            @RequestParam(required = false) Long admissionId
    ) {
        return admissionService.meritStatus(email, admissionId);
    }
}

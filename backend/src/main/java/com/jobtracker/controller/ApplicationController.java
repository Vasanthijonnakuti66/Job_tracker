package com.jobtracker.controller;

import com.jobtracker.dto.ApplicationRequest;
import com.jobtracker.dto.ApplicationResponse;
import com.jobtracker.dto.PagedResponse;
import com.jobtracker.dto.StatusChangeRequest;
import com.jobtracker.dto.StatusHistoryResponse;
import com.jobtracker.entity.ApplicationStatus;
import com.jobtracker.security.UserPrincipal;
import com.jobtracker.service.ApplicationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * REST controller for job application CRUD operations.
 *
 * The current user comes from the JWT via @AuthenticationPrincipal,
 * not from a client-supplied header (those can be forged).
 */
@RestController
@RequestMapping("/api/applications")
public class ApplicationController {

    private final ApplicationService applicationService;

    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @PostMapping
    public ResponseEntity<ApplicationResponse> createApplication(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Valid @RequestBody ApplicationRequest request) {

        ApplicationResponse response =
                applicationService.createApplication(currentUser.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<PagedResponse<ApplicationResponse>> getApplications(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "dateApplied") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String company,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) ApplicationStatus status,
            @RequestParam(required = false) LocalDate fromDate,
            @RequestParam(required = false) LocalDate toDate) {

        PagedResponse<ApplicationResponse> response =
                applicationService.getApplicationsByUser(
                        currentUser.getId(), page, size, sortBy, sortDir,
                        company, role, status, fromDate, toDate);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApplicationResponse> changeStatus(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable Long id,
            @Valid @RequestBody StatusChangeRequest request) {
        return ResponseEntity.ok(
                applicationService.changeStatus(currentUser.getId(), id, request));
    }

    @GetMapping("/{id}/status-history")
    public ResponseEntity<List<StatusHistoryResponse>> getStatusHistory(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable Long id) {
        return ResponseEntity.ok(
                applicationService.getStatusHistory(currentUser.getId(), id));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApplicationResponse> getApplication(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable Long id) {

        ApplicationResponse response =
                applicationService.getApplicationById(currentUser.getId(), id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApplicationResponse> updateApplication(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable Long id,
            @Valid @RequestBody ApplicationRequest request) {

        ApplicationResponse response =
                applicationService.updateApplication(currentUser.getId(), id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteApplication(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable Long id) {

        applicationService.deleteApplication(currentUser.getId(), id);
        return ResponseEntity.noContent().build();
    }
}

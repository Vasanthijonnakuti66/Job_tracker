package com.jobtracker.controller;

import com.jobtracker.dto.InterviewRequest;
import com.jobtracker.dto.InterviewResponse;
import com.jobtracker.security.UserPrincipal;
import com.jobtracker.service.InterviewService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/applications/{applicationId}/interviews")
public class InterviewController {

    private final InterviewService interviewService;

    public InterviewController(InterviewService interviewService) {
        this.interviewService = interviewService;
    }

    @GetMapping
    public ResponseEntity<List<InterviewResponse>> getInterviews(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable Long applicationId) {
        return ResponseEntity.ok(
                interviewService.getInterviews(currentUser.getId(), applicationId));
    }

    @PostMapping
    public ResponseEntity<InterviewResponse> createInterview(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable Long applicationId,
            @Valid @RequestBody InterviewRequest request) {
        InterviewResponse response =
                interviewService.createInterview(currentUser.getId(), applicationId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{interviewId}")
    public ResponseEntity<InterviewResponse> updateInterview(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable Long applicationId,
            @PathVariable Long interviewId,
            @Valid @RequestBody InterviewRequest request) {
        return ResponseEntity.ok(interviewService.updateInterview(
                currentUser.getId(), applicationId, interviewId, request));
    }

    @DeleteMapping("/{interviewId}")
    public ResponseEntity<Void> deleteInterview(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable Long applicationId,
            @PathVariable Long interviewId) {
        interviewService.deleteInterview(currentUser.getId(), applicationId, interviewId);
        return ResponseEntity.noContent().build();
    }
}

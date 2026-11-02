package com.jobtracker.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * DTO for creating or updating a job application.
 *
 * WHY a separate DTO instead of accepting the Application entity directly?
 *
 *   1. SECURITY — The entity has fields the client shouldn't control
 *      (id, user, createdAt, status). A DTO exposes only writable fields.
 *
 *   2. VALIDATION — We put @NotBlank, @Size, etc. on the DTO, not the
 *      entity. This separates "HTTP input rules" from "database rules."
 *
 *   3. DECOUPLING — If we rename a database column, the API contract
 *      doesn't change (the DTO stays the same). Clients don't break.
 *
 * WHY one request DTO for both create and update?
 *   → They need the same fields and same validation rules right now.
 *   → If they diverge later (e.g., "dateApplied" becomes read-only on
 *     update), we'll split into CreateApplicationRequest + UpdateApplicationRequest.
 *
 * BEAN VALIDATION ANNOTATIONS:
 *   @NotBlank  — rejects null, empty "", and whitespace-only "   " strings
 *   @NotNull   — rejects null (for non-String types like LocalDate)
 *   @Size      — enforces min/max length
 *   @Positive  — value must be > 0
 *
 *   These are checked automatically when @Valid is on the controller parameter.
 *   If validation fails, Spring throws MethodArgumentNotValidException,
 *   which our GlobalExceptionHandler catches and turns into a 400 response.
 */
public record ApplicationRequest(

        @NotBlank(message = "Company name is required")
        @Size(max = 255, message = "Company name must be 255 characters or less")
        String companyName,

        @NotBlank(message = "Role is required")
        @Size(max = 255, message = "Role must be 255 characters or less")
        String role,

        @Size(max = 255, message = "Location must be 255 characters or less")
        String location,

        @Size(max = 500, message = "Job link must be 500 characters or less")
        String jobLink,

        @Positive(message = "Minimum salary must be positive")
        Integer salaryMin,

        @Positive(message = "Maximum salary must be positive")
        Integer salaryMax,

        String notes,

        @NotNull(message = "Date applied is required")
        LocalDate dateApplied
) {
    /**
     * Cross-field check: salaryMin cannot be greater than salaryMax.
     * Bean Validation runs this because of @AssertTrue on a boolean method.
     * Both fields are optional, so the rule only applies when both are sent.
     */
    @AssertTrue(message = "Minimum salary cannot be greater than maximum salary")
    public boolean isSalaryRangeValid() {
        if (salaryMin == null || salaryMax == null) {
            return true;
        }
        return salaryMin <= salaryMax;
    }
}

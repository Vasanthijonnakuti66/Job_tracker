package com.jobtracker.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Standardized error response returned by the GlobalExceptionHandler.
 *
 * Every error from our API has the same structure, making it easy for
 * frontend developers to parse errors consistently:
 *
 *   {
 *     "status": 400,
 *     "message": "Validation failed",
 *     "timestamp": "2024-01-15T10:30:00",
 *     "errors": {
 *       "companyName": "Company name is required",
 *       "dateApplied": "Date applied is required"
 *     }
 *   }
 *
 * @JsonInclude(NON_NULL) — omits the "errors" field from the JSON
 * when it's null (i.e., for non-validation errors like 404 or 500).
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        int status,
        String message,
        LocalDateTime timestamp,
        Map<String, String> errors
) {
    /**
     * Convenience constructor for errors without field-level details.
     */
    public ErrorResponse(int status, String message) {
        this(status, message, LocalDateTime.now(), null);
    }
}

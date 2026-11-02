package com.jobtracker.dto;

import com.jobtracker.entity.Application;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO returned by Application API endpoints.
 *
 * ENTITY vs DTO in API RESPONSES:
 *
 *   If we returned the Application entity directly, Jackson would try to
 *   serialize EVERYTHING — including the User object (with its password hash!)
 *   and the lazy-loaded collections (triggering extra SQL queries or errors).
 *
 *   The response DTO lets us control exactly what the client sees.
 *   The static from() method is a factory that converts entity → DTO.
 *
 *   Entity:  Application { id, user, companyName, status, statusHistories... }
 *   DTO:     ApplicationResponse { id, companyName, status, ... }
 *                                  ↑ no user password, no lazy collections
 */
public record ApplicationResponse(
        Long id,
        String companyName,
        String role,
        String location,
        String jobLink,
        Integer salaryMin,
        Integer salaryMax,
        String notes,
        String status,
        LocalDate dateApplied,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    /**
     * Factory method: converts a JPA entity into an API response DTO.
     * Called in the service layer after saving/loading from the database.
     */
    public static ApplicationResponse from(Application app) {
        return new ApplicationResponse(
                app.getId(),
                app.getCompanyName(),
                app.getRole(),
                app.getLocation(),
                app.getJobLink(),
                app.getSalaryMin(),
                app.getSalaryMax(),
                app.getNotes(),
                app.getStatus().name(),     // Enum → String (e.g., "APPLIED")
                app.getDateApplied(),
                app.getCreatedAt(),
                app.getUpdatedAt()
        );
    }
}

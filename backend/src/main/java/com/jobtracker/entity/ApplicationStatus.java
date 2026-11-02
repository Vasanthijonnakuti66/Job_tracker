package com.jobtracker.entity;

/**
 * The stages a job application can move through.
 *
 * This enum is used in BOTH the Java code (@Enumerated) and the database
 * (CHECK constraint). If you add a new status here, you MUST also write
 * a Flyway migration to update the CHECK constraint — otherwise the DB
 * will reject the new value.
 *
 * WHY an enum instead of free-text String?
 *   → Compile-time safety: typos like "OFFFER" are caught by the compiler
 *   → Self-documenting: you can see all valid statuses in one place
 *   → Switch statements get exhaustiveness checking
 */
public enum ApplicationStatus {
    APPLIED,        // Just submitted the application
    ASSESSMENT,     // Online assessment / coding challenge
    INTERVIEW,      // Invited to interview (phone, video, onsite)
    OFFER,          // Received a job offer
    REJECTED        // Rejected at any stage
}

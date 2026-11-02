package com.jobtracker.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * JPA entity mapped to the "status_history" table.
 *
 * This is an APPEND-ONLY audit log. Every time an application's status
 * changes, we INSERT a new row here (we never UPDATE or DELETE).
 *
 * Example timeline for one application:
 *   id=1  status=APPLIED     changed_at=2024-01-15 10:00
 *   id=2  status=ASSESSMENT  changed_at=2024-01-20 14:30
 *   id=3  status=INTERVIEW   changed_at=2024-02-01 09:00
 *   id=4  status=REJECTED    changed_at=2024-02-10 16:45
 *
 * This data powers analytics like "average days from APPLIED to INTERVIEW."
 */
@Entity
@Table(name = "status_history")
public class StatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Back-reference to the parent Application.
    // This is the "owning side" of the relationship because this table
    // holds the FK column (application_id).
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", nullable = false)
    private Application application;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ApplicationStatus status;

    @Column(name = "changed_at", nullable = false)
    private LocalDateTime changedAt;

    @Column(columnDefinition = "TEXT")
    private String notes;

    // --- Constructors ---

    public StatusHistory() {
    }

    public StatusHistory(Application application, ApplicationStatus status, String notes) {
        this.application = application;
        this.status = status;
        this.changedAt = LocalDateTime.now();
        this.notes = notes;
    }

    // --- Getters and Setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Application getApplication() {
        return application;
    }

    public void setApplication(Application application) {
        this.application = application;
    }

    public ApplicationStatus getStatus() {
        return status;
    }

    public void setStatus(ApplicationStatus status) {
        this.status = status;
    }

    public LocalDateTime getChangedAt() {
        return changedAt;
    }

    public void setChangedAt(LocalDateTime changedAt) {
        this.changedAt = changedAt;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}

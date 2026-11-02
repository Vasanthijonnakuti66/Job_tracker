package com.jobtracker.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * JPA entity mapped to the "applications" table.
 * This is the central entity — status history, interviews, and attachments
 * all hang off an application.
 *
 * KEY CONCEPT — @ManyToOne vs @OneToMany:
 *
 *   Application → User  is @ManyToOne  (many applications belong to one user)
 *   Application → StatusHistory is @OneToMany (one application has many history entries)
 *
 *   @ManyToOne = "this entity holds the FK column" (the owning side)
 *   @OneToMany = "the other entity holds the FK column" (the inverse side, uses mappedBy)
 *
 * KEY CONCEPT — FetchType.LAZY:
 *
 *   @OneToMany defaults to LAZY, meaning the list of status histories is NOT
 *   loaded from the DB until you call getStatusHistories(). This prevents
 *   loading the entire object graph when you only need the application itself.
 *
 *   @ManyToOne defaults to EAGER, which we override to LAZY as well.
 *   Rule: start with everything LAZY, then fetch eagerly only when needed
 *   (via JOIN FETCH in JPQL or @EntityGraph).
 *
 * KEY CONCEPT — CascadeType.ALL + orphanRemoval:
 *
 *   CascadeType.ALL means: when you save/update/delete an Application,
 *   Hibernate automatically does the same to its children (history, etc.).
 *
 *   orphanRemoval = true means: if you remove a StatusHistory from the
 *   list, Hibernate will DELETE it from the DB (not just set the FK to null).
 */
@Entity
@Table(name = "applications")
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The user who created this application.
    // LAZY fetch: we don't always need the full User object just to list applications.
    // @JoinColumn tells Hibernate which FK column in the "applications" table
    // maps to the "users" table's PK.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "company_name", nullable = false)
    private String companyName;

    // Column named "job_role" in DB to avoid any SQL keyword issues with "role"
    @Column(name = "job_role", nullable = false)
    private String role;

    private String location;

    @Column(name = "job_link")
    private String jobLink;

    @Column(name = "salary_min")
    private Integer salaryMin;

    @Column(name = "salary_max")
    private Integer salaryMax;

    @Column(columnDefinition = "TEXT")
    private String notes;

    // @Enumerated(STRING) stores the enum NAME as a string in the DB ("APPLIED"),
    // not the ordinal position (0, 1, 2...).
    // WHY STRING over ORDINAL?
    //   → If you reorder or insert enum values, ordinals shift and break existing data.
    //   → String values are human-readable in SQL queries and DB tools.
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ApplicationStatus status = ApplicationStatus.APPLIED;

    @Column(name = "date_applied", nullable = false)
    private LocalDate dateApplied;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // --- Child collections ---
    // mappedBy = "application" refers to the field name in StatusHistory/Interview/Attachment
    // that holds the @ManyToOne back-reference to this Application.

    @OneToMany(mappedBy = "application", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StatusHistory> statusHistories = new ArrayList<>();

    @OneToMany(mappedBy = "application", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Interview> interviews = new ArrayList<>();

    @OneToMany(mappedBy = "application", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Attachment> attachments = new ArrayList<>();

    // --- Lifecycle callbacks ---

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // --- Constructors ---

    public Application() {
    }

    // --- Getters and Setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getJobLink() {
        return jobLink;
    }

    public void setJobLink(String jobLink) {
        this.jobLink = jobLink;
    }

    public Integer getSalaryMin() {
        return salaryMin;
    }

    public void setSalaryMin(Integer salaryMin) {
        this.salaryMin = salaryMin;
    }

    public Integer getSalaryMax() {
        return salaryMax;
    }

    public void setSalaryMax(Integer salaryMax) {
        this.salaryMax = salaryMax;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public ApplicationStatus getStatus() {
        return status;
    }

    public void setStatus(ApplicationStatus status) {
        this.status = status;
    }

    public LocalDate getDateApplied() {
        return dateApplied;
    }

    public void setDateApplied(LocalDate dateApplied) {
        this.dateApplied = dateApplied;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public List<StatusHistory> getStatusHistories() {
        return statusHistories;
    }

    public List<Interview> getInterviews() {
        return interviews;
    }

    public List<Attachment> getAttachments() {
        return attachments;
    }
}

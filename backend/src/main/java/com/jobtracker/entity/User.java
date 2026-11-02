package com.jobtracker.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * JPA entity mapped to the "users" table.
 *
 * KEY JPA CONCEPTS:
 *
 * @Entity     — Tells Hibernate "this class maps to a database table."
 *               Hibernate creates a mapping between fields and columns.
 *
 * @Table      — Specifies the exact table name. Without it, Hibernate
 *               would use the class name "User", but "user" is a reserved
 *               word in some SQL dialects, so we explicitly say "users".
 *
 * @Id         — Marks the primary key field.
 *
 * @GeneratedValue(IDENTITY) — Tells Hibernate the DB generates the ID
 *               (via BIGSERIAL in PostgreSQL = auto-incrementing bigint).
 *               Hibernate reads back the generated ID after INSERT.
 *
 * @Column     — Customizes column mapping (nullable, unique, length).
 *               Fields without @Column still map to columns using the
 *               default naming strategy (camelCase → snake_case).
 *
 * WHY no @OneToMany for applications here?
 *   → We query applications by user_id through the repository, not by
 *     navigating user.getApplications(). This avoids accidentally loading
 *     hundreds of applications when we only need the user's email.
 *   → Rule of thumb: only add collection mappings when you genuinely
 *     need to navigate that direction in business logic.
 */
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    // Stores the bcrypt hash, never the plain-text password.
    // @JsonIgnore is a last-line defense if someone accidentally returns a User entity.
    @JsonIgnore
    @Column(nullable = false)
    private String password;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // --- Lifecycle callbacks ---
    // These run automatically before Hibernate executes INSERT/UPDATE SQL.
    // They keep timestamp logic in one place instead of scattered across services.

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
    // JPA requires a no-arg constructor (it creates instances via reflection).
    // The parameterized constructor is for our convenience in application code.

    public User() {
    }

    public User(String email, String password, String fullName) {
        this.email = email;
        this.password = password;
        this.fullName = fullName;
    }

    // --- Getters and Setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}

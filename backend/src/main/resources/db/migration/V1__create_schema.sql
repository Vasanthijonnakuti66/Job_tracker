-- ============================================================================
-- V1: Initial schema for Job Tracker
-- ============================================================================
-- Flyway naming convention: V<version>__<description>.sql
--   V1  = version 1 (run first)
--   __  = double underscore separator
--   Flyway tracks which versions have run in the flyway_schema_history table,
--   so it never re-runs a migration that has already been applied.
-- ============================================================================


-- =============================================
-- USERS: registered job seekers
-- =============================================
-- Each user has a unique email (used as login) and a hashed password.
-- We'll store bcrypt hashes in Phase 4 (security).
CREATE TABLE users (
    id          BIGSERIAL       PRIMARY KEY,
    email       VARCHAR(255)    NOT NULL,
    password    VARCHAR(255)    NOT NULL,
    full_name   VARCHAR(255)    NOT NULL,
    created_at  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- UNIQUE constraint also creates an index, so login lookups are fast
    CONSTRAINT uq_users_email UNIQUE (email)
);


-- =============================================
-- APPLICATIONS: one row per job application
-- =============================================
-- Central table — everything else (history, interviews, attachments) links here.
-- user_id ensures every application belongs to exactly one user.
CREATE TABLE applications (
    id              BIGSERIAL       PRIMARY KEY,
    user_id         BIGINT          NOT NULL,
    company_name    VARCHAR(255)    NOT NULL,
    job_role        VARCHAR(255)    NOT NULL,
    location        VARCHAR(255),
    job_link        VARCHAR(500),
    salary_min      INTEGER,
    salary_max      INTEGER,
    notes           TEXT,
    status          VARCHAR(50)     NOT NULL DEFAULT 'APPLIED',
    date_applied    DATE            NOT NULL,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- FK: if a user is deleted, all their applications go with them
    CONSTRAINT fk_applications_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,

    -- Only allow valid statuses at the database level (defense in depth)
    CONSTRAINT chk_applications_status
        CHECK (status IN ('APPLIED', 'ASSESSMENT', 'INTERVIEW', 'OFFER', 'REJECTED')),

    -- Salary range sanity: min can't exceed max (both are optional)
    CONSTRAINT chk_applications_salary
        CHECK (salary_min IS NULL OR salary_max IS NULL OR salary_min <= salary_max)
);

-- Almost every query is "show me MY applications", so this index is critical
CREATE INDEX idx_applications_user_id ON applications(user_id);

-- Composite indexes for common filter patterns (status pipeline, date filtering)
CREATE INDEX idx_applications_user_status ON applications(user_id, status);
CREATE INDEX idx_applications_user_date ON applications(user_id, date_applied);


-- =============================================
-- STATUS_HISTORY: audit trail of status changes
-- =============================================
-- Every time an application moves stages (e.g., APPLIED → INTERVIEW),
-- we INSERT a row here. This gives us:
--   1. A complete timeline of each application
--   2. Data for analytics (avg days per stage, conversion rates)
-- We never UPDATE or DELETE these rows — they're an append-only log.
CREATE TABLE status_history (
    id              BIGSERIAL       PRIMARY KEY,
    application_id  BIGINT          NOT NULL,
    status          VARCHAR(50)     NOT NULL,
    changed_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    notes           TEXT,

    CONSTRAINT fk_status_history_application
        FOREIGN KEY (application_id) REFERENCES applications(id) ON DELETE CASCADE,

    CONSTRAINT chk_status_history_status
        CHECK (status IN ('APPLIED', 'ASSESSMENT', 'INTERVIEW', 'OFFER', 'REJECTED'))
);

-- We frequently load the full history for one application
CREATE INDEX idx_status_history_app_id ON status_history(application_id);


-- =============================================
-- INTERVIEWS: multiple interviews per application
-- =============================================
-- A single job application might involve a phone screen, a technical
-- round, and an HR round — each is its own row here.
CREATE TABLE interviews (
    id              BIGSERIAL       PRIMARY KEY,
    application_id  BIGINT          NOT NULL,
    interview_date  DATE            NOT NULL,
    interview_time  TIME,
    type            VARCHAR(50)     NOT NULL,
    location        VARCHAR(255),
    notes           TEXT,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_interviews_application
        FOREIGN KEY (application_id) REFERENCES applications(id) ON DELETE CASCADE,

    CONSTRAINT chk_interviews_type
        CHECK (type IN ('PHONE', 'VIDEO', 'ONSITE', 'TECHNICAL', 'HR', 'OTHER'))
);

CREATE INDEX idx_interviews_app_id ON interviews(application_id);

-- For "upcoming interviews this week" dashboard query
CREATE INDEX idx_interviews_date ON interviews(interview_date);


-- =============================================
-- ATTACHMENTS: resume / JD files per application
-- =============================================
-- We store the file on disk (not in the DB) and track metadata here.
-- file_path points to the location on the server's filesystem.
CREATE TABLE attachments (
    id              BIGSERIAL       PRIMARY KEY,
    application_id  BIGINT          NOT NULL,
    file_name       VARCHAR(255)    NOT NULL,
    file_path       VARCHAR(500)    NOT NULL,
    file_type       VARCHAR(50)     NOT NULL,
    file_size       BIGINT,
    uploaded_at     TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_attachments_application
        FOREIGN KEY (application_id) REFERENCES applications(id) ON DELETE CASCADE,

    CONSTRAINT chk_attachments_file_type
        CHECK (file_type IN ('RESUME', 'JOB_DESCRIPTION'))
);

CREATE INDEX idx_attachments_app_id ON attachments(application_id);

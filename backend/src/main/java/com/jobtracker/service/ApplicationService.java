package com.jobtracker.service;

import com.jobtracker.dto.ApplicationRequest;
import com.jobtracker.dto.ApplicationResponse;
import com.jobtracker.dto.StatusChangeRequest;
import com.jobtracker.dto.StatusHistoryResponse;
import com.jobtracker.dto.PagedResponse;
import com.jobtracker.entity.Application;
import com.jobtracker.entity.ApplicationStatus;
import com.jobtracker.entity.StatusHistory;
import com.jobtracker.entity.User;
import com.jobtracker.exception.ResourceNotFoundException;
import com.jobtracker.repository.ApplicationRepository;
import com.jobtracker.repository.StatusHistoryRepository;
import com.jobtracker.repository.UserRepository;
import com.jobtracker.service.AttachmentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

/**
 * Service layer for job application CRUD operations.
 *
 * THE SERVICE LAYER'S JOB:
 *   1. Orchestrate business logic (validation beyond bean validation)
 *   2. Call repositories to read/write data
 *   3. Convert between entities and DTOs
 *   4. Manage transactions
 *
 * WHY NOT put this logic in the controller?
 *   → Controllers should only handle HTTP concerns (read params, return responses).
 *   → Business logic in a service is reusable: a scheduled job, a message
 *     consumer, or another service can call the same methods.
 *   → Services are easier to unit test (no HTTP mocking needed).
 *
 * @Service — tells Spring to create a singleton bean of this class and
 *            register it in the application context for dependency injection.
 *
 * @Transactional(readOnly = true) — class-level default:
 *   - Every method runs inside a database transaction
 *   - "readOnly" is a hint to Hibernate: skip dirty checking for read-only
 *     methods, which improves performance
 *   - Write methods override this with @Transactional (readOnly = false)
 */
@Service
@Transactional(readOnly = true)
public class ApplicationService {

    private static final Logger log = LoggerFactory.getLogger(ApplicationService.class);

    // Only entity field names are allowed in ORDER BY. Without this whitelist,
    // a client could send sortBy=unknownField and Hibernate would throw a
    // hard-to-read 500 instead of a clear 400.
    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "id", "companyName", "role", "location", "status",
            "dateApplied", "salaryMin", "salaryMax", "createdAt", "updatedAt"
    );

    private final ApplicationRepository applicationRepository;
    private final UserRepository userRepository;
    private final StatusHistoryRepository statusHistoryRepository;
    private final AttachmentService attachmentService;

    // Constructor injection keeps each required service dependency explicit.
    public ApplicationService(ApplicationRepository applicationRepository,
                              UserRepository userRepository,
                              StatusHistoryRepository statusHistoryRepository,
                              AttachmentService attachmentService) {
        this.applicationRepository = applicationRepository;
        this.userRepository = userRepository;
        this.statusHistoryRepository = statusHistoryRepository;
        this.attachmentService = attachmentService;
    }

    /**
     * Create a new job application for the given user.
     *
     * TRANSACTION FLOW:
     *   1. Spring opens a transaction (BEGIN)
     *   2. We save the Application → INSERT INTO applications
     *   3. We save the StatusHistory → INSERT INTO status_history
     *   4. Method returns successfully → Spring commits (COMMIT)
     *   5. If any exception occurs → Spring rolls back (ROLLBACK)
     *
     *   This ensures both INSERTs succeed or neither does (atomicity).
     */
    @Transactional  // Overrides class-level readOnly=true — this method writes
    public ApplicationResponse createApplication(Long userId, ApplicationRequest request) {
        // Look up the user — throw 404 if not found
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // Map DTO → Entity
        Application app = new Application();
        app.setUser(user);
        app.setCompanyName(request.companyName());
        app.setRole(request.role());
        app.setLocation(request.location());
        app.setJobLink(request.jobLink());
        app.setSalaryMin(request.salaryMin());
        app.setSalaryMax(request.salaryMax());
        app.setNotes(request.notes());
        app.setStatus(ApplicationStatus.APPLIED);  // Always starts as APPLIED
        app.setDateApplied(request.dateApplied());

        Application saved = applicationRepository.save(app);

        // Record the initial status in the audit log.
        // Every application starts as APPLIED, so we log that event.
        StatusHistory initialStatus = new StatusHistory(
                saved, ApplicationStatus.APPLIED, "Application created"
        );
        statusHistoryRepository.save(initialStatus);

        log.info("Created application id={} for user id={}", saved.getId(), userId);

        // Map Entity → DTO for the response
        return ApplicationResponse.from(saved);
    }

    /**
     * Get a single application by ID (only if it belongs to the given user).
     */
    public ApplicationResponse getApplicationById(Long userId, Long applicationId) {
        Application app = findUserApplication(userId, applicationId);
        return ApplicationResponse.from(app);
    }

    /**
     * Get all applications for a user with pagination and sorting.
     *
     * PAGINATION PARAMETERS:
     *   page    — which page to return (0-based)
     *   size    — how many items per page
     *   sortBy  — which field to sort by (e.g., "dateApplied", "companyName")
     *   sortDir — "asc" or "desc"
     *
     * Spring Data's Pageable encapsulates all of these.
     * The repository returns a Page<Application> which includes:
     *   - The data for this page
     *   - Total element count (from a COUNT query)
     *   - Total page count, isLast, etc.
     */
    public PagedResponse<ApplicationResponse> getApplicationsByUser(
            Long userId, int page, int size, String sortBy, String sortDir,
            String company, String role, ApplicationStatus status,
            LocalDate fromDate, LocalDate toDate) {

        if (!ALLOWED_SORT_FIELDS.contains(sortBy)) {
            throw new IllegalArgumentException(
                    "Invalid sort field: " + sortBy + ". Allowed: " + ALLOWED_SORT_FIELDS);
        }
        if (page < 0) {
            throw new IllegalArgumentException("Page must be zero or greater");
        }
        if (size < 1 || size > 100) {
            throw new IllegalArgumentException("Page size must be between 1 and 100");
        }
        if (!sortDir.equalsIgnoreCase("asc") && !sortDir.equalsIgnoreCase("desc")) {
            throw new IllegalArgumentException("Sort direction must be 'asc' or 'desc'");
        }
        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            throw new IllegalArgumentException("Start date must be on or before end date");
        }

        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        // PageRequest implements Pageable — combines page, size, and sort
        Pageable pageable = PageRequest.of(page, size, sort);

        // Execute the query — Spring Data generates:
        //   SELECT * FROM applications WHERE user_id = ?
        //   ORDER BY date_applied DESC
        //   LIMIT 20 OFFSET 0
        Page<Application> applicationPage = applicationRepository.searchByUser(
                userId, normalizeFilter(company), normalizeFilter(role), status,
                fromDate, toDate, pageable);

        // Convert each Application entity to ApplicationResponse DTO.
        // Page.map() returns a new Page with transformed content.
        Page<ApplicationResponse> responsePage = applicationPage.map(ApplicationResponse::from);

        return PagedResponse.from(responsePage);
    }

    public List<StatusHistoryResponse> getStatusHistory(Long userId, Long applicationId) {
        findUserApplication(userId, applicationId);
        return statusHistoryRepository.findByApplicationIdOrderByChangedAtDesc(applicationId)
                .stream()
                .map(StatusHistoryResponse::from)
                .toList();
    }

    @Transactional
    public ApplicationResponse changeStatus(Long userId, Long applicationId,
                                            StatusChangeRequest request) {
        Application app = findUserApplication(userId, applicationId);
        if (app.getStatus() != request.status()) {
            app.setStatus(request.status());
            applicationRepository.save(app);
            statusHistoryRepository.save(new StatusHistory(app, request.status(), request.notes()));
            log.info("Changed application id={} status to {} for user id={}",
                    applicationId, request.status(), userId);
        }
        return ApplicationResponse.from(app);
    }

    /**
     * Update an existing application's details.
     *
     * Note: This does NOT change the status. Status changes go through
     * a dedicated endpoint (Phase 5) that also records history.
     */
    @Transactional
    public ApplicationResponse updateApplication(Long userId, Long applicationId,
                                                  ApplicationRequest request) {
        Application app = findUserApplication(userId, applicationId);

        // Update only the fields that come from the request.
        // Status, user, and timestamps are NOT modifiable through this endpoint.
        app.setCompanyName(request.companyName());
        app.setRole(request.role());
        app.setLocation(request.location());
        app.setJobLink(request.jobLink());
        app.setSalaryMin(request.salaryMin());
        app.setSalaryMax(request.salaryMax());
        app.setNotes(request.notes());
        app.setDateApplied(request.dateApplied());

        // JPA "dirty checking": Hibernate detects changes to managed entities
        // and generates an UPDATE statement automatically at transaction commit.
        // We call save() explicitly for clarity, but it's technically optional
        // for managed entities within a transaction.
        Application updated = applicationRepository.save(app);

        log.info("Updated application id={} for user id={}", applicationId, userId);

        return ApplicationResponse.from(updated);
    }

    /**
     * Delete an application (and its children via CASCADE).
     */
    @Transactional
    public void deleteApplication(Long userId, Long applicationId) {
        Application app = findUserApplication(userId, applicationId);
        attachmentService.deleteFilesAfterApplicationDeleted(app.getAttachments());
        applicationRepository.delete(app);
        log.info("Deleted application id={} for user id={}", applicationId, userId);
    }

    // ─── Private helpers ───────────────────────────────────────────────

    /**
     * Find an application that belongs to a specific user, or throw 404.
     *
     * This method enforces per-user data isolation at the service level:
     * even if someone guesses a valid application ID, they can't access
     * it unless they're the owner.
     */
    private Application findUserApplication(Long userId, Long applicationId) {
        return applicationRepository.findByIdAndUserId(applicationId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Application", "id", applicationId));
    }

    private String normalizeFilter(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}

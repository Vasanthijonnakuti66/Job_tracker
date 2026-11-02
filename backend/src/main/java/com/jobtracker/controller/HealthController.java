package com.jobtracker.controller;

import com.jobtracker.dto.HealthResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;

/**
 * Health-check endpoints for the Job Tracker API.
 *
 * These are the first endpoints every backend project should have:
 *   - /api/health    → "Is the server running?"
 *   - /api/health/db → "Can the server talk to the database?"
 *
 * WHY health endpoints?
 *   → Docker, Kubernetes, and load balancers poll these to decide
 *     whether to route traffic to this instance or restart it.
 *   → During development, they're the quickest way to verify your
 *     setup is working before writing business logic.
 *
 * @RestController = @Controller + @ResponseBody
 *   → Every method's return value is serialized to JSON automatically.
 *   → No need for @ResponseBody on each method.
 *
 * @RequestMapping("/api") sets a base path prefix for all endpoints
 * in this controller, so @GetMapping("/health") becomes GET /api/health.
 */
@RestController
@RequestMapping("/api")
public class HealthController {

    // DataSource is the JDBC connection pool (HikariCP).
    // Spring auto-creates it from the datasource properties in application.yml
    // and injects it here via constructor injection.
    private final DataSource dataSource;

    // Constructor injection is preferred over @Autowired on a field because:
    //   1. Dependencies are explicit and immutable (final)
    //   2. The class can be unit-tested by passing a mock DataSource
    //   3. Spring can detect circular dependencies at startup
    public HealthController(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * GET /api/health
     * Simple liveness probe — confirms the JVM and web server are up.
     * Always returns 200 OK with status "UP".
     */
    @GetMapping("/health")
    public ResponseEntity<HealthResponse> health() {
        return ResponseEntity.ok(
                new HealthResponse("UP", "Job Tracker API is running")
        );
    }

    /**
     * GET /api/health/db
     * Readiness probe — verifies the database connection is alive.
     *
     * HOW IT WORKS:
     *   1. Borrows a connection from the pool (HikariCP)
     *   2. Runs "SELECT 1" — the lightest possible query
     *   3. If it succeeds → 200 OK, status "UP"
     *   4. If it fails   → 503 Service Unavailable, status "DOWN"
     *
     * The try-with-resources block automatically closes (returns) the
     * connection to the pool, even if an exception occurs.
     */
    @GetMapping("/health/db")
    public ResponseEntity<HealthResponse> healthDb() {
        try (Connection conn = dataSource.getConnection()) {
            // Execute the simplest possible query to prove connectivity
            conn.createStatement().execute("SELECT 1");
            return ResponseEntity.ok(
                    new HealthResponse("UP", "Database connection is healthy")
            );
        } catch (Exception e) {
            // Return 503 so load balancers know this instance can't serve requests
            return ResponseEntity
                    .status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(new HealthResponse("DOWN",
                            "Database connection failed: " + e.getMessage()));
        }
    }
}

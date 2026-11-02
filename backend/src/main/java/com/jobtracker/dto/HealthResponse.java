package com.jobtracker.dto;

/**
 * DTO (Data Transfer Object) returned by the health-check endpoints.
 *
 * We use a Java "record" here — a compact way (Java 16+) to define an
 * immutable data carrier. The compiler auto-generates:
 *   - a constructor: new HealthResponse("UP", "message")
 *   - getter methods: status(), message()
 *   - equals(), hashCode(), toString()
 *
 * Jackson (the JSON library) automatically serializes this to:
 *   { "status": "UP", "message": "..." }
 *
 * WHY a DTO instead of a plain Map or String?
 *   → Gives us a typed, self-documenting contract for the API response.
 *   → If we ever need to add fields (e.g., "version"), we change one place.
 */
public record HealthResponse(
        String status,
        String message
) {
}

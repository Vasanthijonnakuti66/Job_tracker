package com.jobtracker.dto;

import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Generic wrapper for paginated API responses.
 *
 * PAGINATION — WHY?
 *   If a user has 500 applications, returning all 500 in one response is:
 *     - Slow (big JSON payload, lots of serialization)
 *     - Memory-hungry (all 500 entities loaded into the JVM at once)
 *     - Bad UX (the frontend doesn't show 500 rows at once anyway)
 *
 *   Instead, we return one "page" at a time (e.g., 20 items) plus metadata
 *   so the frontend can render page controls: "Page 1 of 25  < 1 2 3 ... >"
 *
 * GENERIC TYPE <T>:
 *   This record works for any entity type: PagedResponse<ApplicationResponse>,
 *   PagedResponse<InterviewResponse>, etc. The <T> is resolved at compile time.
 *
 * @param content       — The actual items on this page
 * @param page          — Current page number (0-based)
 * @param size          — Number of items per page
 * @param totalElements — Total items across all pages
 * @param totalPages    — Total number of pages
 * @param last          — true if this is the final page
 */
public record PagedResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean last
) {
    /**
     * Converts a Spring Data Page<T> into our API response format.
     *
     * Usage:
     *   Page<ApplicationResponse> springPage = ...;
     *   PagedResponse<ApplicationResponse> apiResponse = PagedResponse.from(springPage);
     */
    public static <T> PagedResponse<T> from(Page<T> springPage) {
        return new PagedResponse<>(
                springPage.getContent(),
                springPage.getNumber(),
                springPage.getSize(),
                springPage.getTotalElements(),
                springPage.getTotalPages(),
                springPage.isLast()
        );
    }
}

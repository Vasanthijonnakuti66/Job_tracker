package com.jobtracker.dto;

import com.jobtracker.entity.StatusHistory;

import java.time.LocalDateTime;

public record StatusHistoryResponse(
        Long id,
        String status,
        LocalDateTime changedAt,
        String notes
) {
    public static StatusHistoryResponse from(StatusHistory history) {
        return new StatusHistoryResponse(
                history.getId(),
                history.getStatus().name(),
                history.getChangedAt(),
                history.getNotes()
        );
    }
}

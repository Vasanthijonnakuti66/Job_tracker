package com.jobtracker.dto;

import com.jobtracker.entity.ApplicationStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record StatusChangeRequest(
        @NotNull(message = "Status is required")
        ApplicationStatus status,

        @Size(max = 2000, message = "Status notes must be 2000 characters or less")
        String notes
) {
}

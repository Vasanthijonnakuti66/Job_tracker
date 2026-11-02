package com.jobtracker.dto;

import com.jobtracker.entity.InterviewType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalTime;

public record InterviewRequest(
        @NotNull(message = "Interview date is required")
        LocalDate interviewDate,

        LocalTime interviewTime,

        @NotNull(message = "Interview type is required")
        InterviewType type,

        @Size(max = 255, message = "Location must be 255 characters or less")
        String location,

        @Size(max = 2000, message = "Interview notes must be 2000 characters or less")
        String notes
) {
}

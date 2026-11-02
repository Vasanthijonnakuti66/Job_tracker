package com.jobtracker.dto;

import com.jobtracker.entity.Interview;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record InterviewResponse(
        Long id,
        Long applicationId,
        LocalDate interviewDate,
        LocalTime interviewTime,
        String type,
        String location,
        String notes,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static InterviewResponse from(Interview interview) {
        return new InterviewResponse(
                interview.getId(),
                interview.getApplication().getId(),
                interview.getInterviewDate(),
                interview.getInterviewTime(),
                interview.getType().name(),
                interview.getLocation(),
                interview.getNotes(),
                interview.getCreatedAt(),
                interview.getUpdatedAt()
        );
    }
}

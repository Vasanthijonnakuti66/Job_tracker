package com.jobtracker.service;

import com.jobtracker.dto.InterviewRequest;
import com.jobtracker.entity.Application;
import com.jobtracker.entity.Interview;
import com.jobtracker.entity.InterviewType;
import com.jobtracker.exception.ResourceNotFoundException;
import com.jobtracker.repository.ApplicationRepository;
import com.jobtracker.repository.InterviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InterviewServiceTest {

    @Mock
    private ApplicationRepository applicationRepository;
    @Mock
    private InterviewRepository interviewRepository;

    private InterviewService service;

    @BeforeEach
    void setUp() {
        service = new InterviewService(applicationRepository, interviewRepository);
    }

    @Test
    void reschedulingInterviewClearsPreviouslySentReminder() {
        Interview interview = interview(12L, LocalDateTime.of(2026, 10, 1, 10, 0));
        when(interviewRepository.findOwnedInterview(12L, 21L, 7L))
                .thenReturn(Optional.of(interview));
        when(interviewRepository.save(interview)).thenReturn(interview);

        service.updateInterview(7L, 21L, 12L, request(LocalDate.of(2026, 10, 8)));

        assertNull(interview.getReminderSentAt());
        assertEquals(LocalDate.of(2026, 10, 8), interview.getInterviewDate());
        verify(interviewRepository).save(interview);
    }

    @Test
    void changingInterviewNotesDoesNotClearReminder() {
        LocalDateTime reminderSentAt = LocalDateTime.of(2026, 10, 1, 10, 0);
        Interview interview = interview(12L, reminderSentAt);
        when(interviewRepository.findOwnedInterview(12L, 21L, 7L))
                .thenReturn(Optional.of(interview));
        when(interviewRepository.save(interview)).thenReturn(interview);

        service.updateInterview(7L, 21L, 12L, request(interview.getInterviewDate()));

        assertEquals(reminderSentAt, interview.getReminderSentAt());
    }

    @Test
    void updateRejectsInterviewNotOwnedByUser() {
        when(interviewRepository.findOwnedInterview(12L, 21L, 7L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                service.updateInterview(7L, 21L, 12L, request(LocalDate.of(2026, 10, 8))));

        verify(interviewRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    private Interview interview(Long id, LocalDateTime reminderSentAt) {
        Application application = new Application();
        application.setId(21L);
        Interview interview = new Interview();
        interview.setId(id);
        interview.setApplication(application);
        interview.setInterviewDate(LocalDate.of(2026, 10, 5));
        interview.setInterviewTime(LocalTime.of(11, 0));
        interview.setType(InterviewType.VIDEO);
        interview.setReminderSentAt(reminderSentAt);
        return interview;
    }

    private InterviewRequest request(LocalDate date) {
        return new InterviewRequest(date, LocalTime.of(11, 0), InterviewType.VIDEO,
                "Online", "Updated notes");
    }
}

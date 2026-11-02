package com.jobtracker.service;

import com.jobtracker.dto.ApplicationRequest;
import com.jobtracker.dto.ApplicationResponse;
import com.jobtracker.dto.StatusChangeRequest;
import com.jobtracker.entity.Application;
import com.jobtracker.entity.ApplicationStatus;
import com.jobtracker.entity.StatusHistory;
import com.jobtracker.entity.User;
import com.jobtracker.exception.ResourceNotFoundException;
import com.jobtracker.repository.ApplicationRepository;
import com.jobtracker.repository.StatusHistoryRepository;
import com.jobtracker.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApplicationServiceTest {

    @Mock
    private ApplicationRepository applicationRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private StatusHistoryRepository statusHistoryRepository;
    @Mock
    private AttachmentService attachmentService;

    private ApplicationService service;

    @BeforeEach
    void setUp() {
        service = new ApplicationService(
                applicationRepository, userRepository, statusHistoryRepository, attachmentService);
    }

    @Test
    void createApplicationStartsAtAppliedAndRecordsInitialHistory() {
        User user = new User("student@example.com", "hashed", "Student");
        user.setId(7L);
        when(userRepository.findById(7L)).thenReturn(Optional.of(user));
        when(applicationRepository.save(any(Application.class))).thenAnswer(invocation -> {
            Application application = invocation.getArgument(0);
            application.setId(21L);
            return application;
        });

        ApplicationRequest request = new ApplicationRequest(
                "Northstar", "Engineer", "Remote", null, null, null,
                "Referred by a friend", LocalDate.of(2026, 10, 5));

        ApplicationResponse response = service.createApplication(7L, request);

        assertEquals(21L, response.id());
        assertEquals("APPLIED", response.status());
        assertEquals(LocalDate.of(2026, 10, 5), response.dateApplied());
        ArgumentCaptor<StatusHistory> historyCaptor = ArgumentCaptor.forClass(StatusHistory.class);
        verify(statusHistoryRepository).save(historyCaptor.capture());
        assertEquals(ApplicationStatus.APPLIED, historyCaptor.getValue().getStatus());
        assertEquals("Application created", historyCaptor.getValue().getNotes());
    }

    @Test
    void changeStatusUpdatesApplicationAndRecordsTransition() {
        Application application = application(21L, ApplicationStatus.APPLIED);
        when(applicationRepository.findByIdAndUserId(21L, 7L)).thenReturn(Optional.of(application));
        when(applicationRepository.save(application)).thenReturn(application);

        ApplicationResponse response = service.changeStatus(
                7L, 21L, new StatusChangeRequest(ApplicationStatus.INTERVIEW, "Onsite round"));

        assertEquals("INTERVIEW", response.status());
        verify(applicationRepository).save(application);
        ArgumentCaptor<StatusHistory> historyCaptor = ArgumentCaptor.forClass(StatusHistory.class);
        verify(statusHistoryRepository).save(historyCaptor.capture());
        assertEquals(ApplicationStatus.INTERVIEW, historyCaptor.getValue().getStatus());
        assertEquals("Onsite round", historyCaptor.getValue().getNotes());
    }

    @Test
    void changingToCurrentStatusDoesNotAddDuplicateHistory() {
        Application application = application(21L, ApplicationStatus.INTERVIEW);
        when(applicationRepository.findByIdAndUserId(21L, 7L)).thenReturn(Optional.of(application));

        ApplicationResponse response = service.changeStatus(
                7L, 21L, new StatusChangeRequest(ApplicationStatus.INTERVIEW, "No change"));

        assertEquals("INTERVIEW", response.status());
        verify(applicationRepository, never()).save(any(Application.class));
        verify(statusHistoryRepository, never()).save(any(StatusHistory.class));
    }

    @Test
    void changeStatusHidesApplicationsOwnedByAnotherUser() {
        when(applicationRepository.findByIdAndUserId(21L, 7L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.changeStatus(
                7L, 21L, new StatusChangeRequest(ApplicationStatus.OFFER, null)));

        verify(statusHistoryRepository, never()).save(any(StatusHistory.class));
    }

    @Test
    void listingRejectsUnsafeSortFieldBeforeQueryingRepository() {
        assertThrows(IllegalArgumentException.class, () -> service.getApplicationsByUser(
                7L, 0, 10, "user.password", "asc", null, null,
                null, null, null));

        verify(applicationRepository, never()).searchByUser(
                any(), any(), any(), any(), any(), any(), any());
    }

    private Application application(Long id, ApplicationStatus status) {
        Application application = new Application();
        application.setId(id);
        application.setStatus(status);
        application.setCompanyName("Northstar");
        application.setRole("Engineer");
        application.setDateApplied(LocalDate.of(2026, 10, 5));
        return application;
    }
}

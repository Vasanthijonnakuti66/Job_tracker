package com.jobtracker.service;

import com.jobtracker.dto.InterviewRequest;
import com.jobtracker.dto.InterviewResponse;
import com.jobtracker.entity.Application;
import com.jobtracker.entity.Interview;
import com.jobtracker.exception.ResourceNotFoundException;
import com.jobtracker.repository.ApplicationRepository;
import com.jobtracker.repository.InterviewRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class InterviewService {

    private final ApplicationRepository applicationRepository;
    private final InterviewRepository interviewRepository;

    public InterviewService(ApplicationRepository applicationRepository,
                            InterviewRepository interviewRepository) {
        this.applicationRepository = applicationRepository;
        this.interviewRepository = interviewRepository;
    }

    public List<InterviewResponse> getInterviews(Long userId, Long applicationId) {
        findUserApplication(userId, applicationId);
        return interviewRepository.findByApplicationIdOrderByInterviewDateAsc(applicationId)
                .stream()
                .map(InterviewResponse::from)
                .toList();
    }

    @Transactional
    public InterviewResponse createInterview(Long userId, Long applicationId,
                                             InterviewRequest request) {
        Application application = findUserApplication(userId, applicationId);
        Interview interview = new Interview();
        interview.setApplication(application);
        applyRequest(interview, request);
        return InterviewResponse.from(interviewRepository.save(interview));
    }

    @Transactional
    public InterviewResponse updateInterview(Long userId, Long applicationId, Long interviewId,
                                             InterviewRequest request) {
        Interview interview = findUserInterview(userId, applicationId, interviewId);
        boolean scheduleChanged = !interview.getInterviewDate().equals(request.interviewDate())
                || !java.util.Objects.equals(interview.getInterviewTime(), request.interviewTime());
        applyRequest(interview, request);
        if (scheduleChanged) {
            interview.setReminderSentAt(null);
        }
        return InterviewResponse.from(interviewRepository.save(interview));
    }

    @Transactional
    public void deleteInterview(Long userId, Long applicationId, Long interviewId) {
        Interview interview = findUserInterview(userId, applicationId, interviewId);
        interviewRepository.delete(interview);
    }

    private Application findUserApplication(Long userId, Long applicationId) {
        return applicationRepository.findByIdAndUserId(applicationId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Application", "id", applicationId));
    }

    private Interview findUserInterview(Long userId, Long applicationId, Long interviewId) {
        return interviewRepository.findOwnedInterview(interviewId, applicationId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Interview", "id", interviewId));
    }

    private void applyRequest(Interview interview, InterviewRequest request) {
        interview.setInterviewDate(request.interviewDate());
        interview.setInterviewTime(request.interviewTime());
        interview.setType(request.type());
        interview.setLocation(request.location());
        interview.setNotes(request.notes());
    }
}

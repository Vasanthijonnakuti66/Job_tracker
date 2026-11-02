package com.jobtracker.service;

import com.jobtracker.entity.Interview;
import com.jobtracker.repository.InterviewRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@ConditionalOnProperty(name = "app.reminders.enabled", havingValue = "true")
public class InterviewReminderService {

    private static final Logger log = LoggerFactory.getLogger(InterviewReminderService.class);
    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("h:mm a");

    private final InterviewRepository interviewRepository;
    private final JavaMailSender mailSender;
    private final String fromAddress;

    public InterviewReminderService(InterviewRepository interviewRepository,
                                    JavaMailSender mailSender,
                                    @Value("${app.reminders.from}") String fromAddress) {
        this.interviewRepository = interviewRepository;
        this.mailSender = mailSender;
        this.fromAddress = fromAddress;
    }

    @Scheduled(cron = "${app.reminders.cron}")
    public void sendUpcomingInterviewReminders() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime end = now.plusHours(24);
        List<Interview> upcoming = interviewRepository.findUnremindedWithinNextDay(
                now.toLocalDate(),
                now.toLocalTime(),
                end.toLocalDate(),
                end.toLocalTime()
        );

        for (Interview interview : upcoming) {
            try {
                mailSender.send(createMessage(interview));
                interview.setReminderSentAt(LocalDateTime.now());
                interviewRepository.save(interview);
                log.info("Sent interview reminder for interview id={}", interview.getId());
            } catch (MailException ex) {
                log.error("Could not send reminder for interview id={} to user id={}",
                        interview.getId(), interview.getApplication().getUser().getId(), ex);
            }
        }
    }

    private SimpleMailMessage createMessage(Interview interview) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(interview.getApplication().getUser().getEmail());
        message.setSubject("Interview reminder: " + interview.getApplication().getCompanyName());

        StringBuilder body = new StringBuilder()
                .append("Your ")
                .append(interview.getType().name().toLowerCase().replace('_', ' '))
                .append(" interview with ")
                .append(interview.getApplication().getCompanyName())
                .append(" for ")
                .append(interview.getApplication().getRole())
                .append(" is coming up on ")
                .append(interview.getInterviewDate().format(DATE_FORMAT));
        LocalTime time = interview.getInterviewTime();
        if (time != null) {
            body.append(" at ").append(time.format(TIME_FORMAT));
        }
        if (interview.getLocation() != null && !interview.getLocation().isBlank()) {
            body.append(". Location: ").append(interview.getLocation());
        }
        body.append(".\n\nGood luck!");
        message.setText(body.toString());
        return message;
    }
}

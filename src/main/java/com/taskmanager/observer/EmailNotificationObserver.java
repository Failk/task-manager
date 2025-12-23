package com.taskmanager.observer;

import com.taskmanager.entity.Notification;
import com.taskmanager.entity.Reminder;
import com.taskmanager.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmailNotificationObserver implements NotificationObserver {

    private final JavaMailSender mailSender;

    @Override
    @Async
    public void notify(User user, Reminder reminder, String message) {
        try {
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setTo(user.getEmail());
            mailMessage.setSubject("Task Reminder: " + reminder.getTask().getTitle());
            mailMessage.setText(buildEmailContent(user, reminder, message));
            
            mailSender.send(mailMessage);
            log.info("Email notification sent to {} for task: {}", 
                    user.getEmail(), reminder.getTask().getTitle());
        } catch (Exception e) {
            log.error("Failed to send email notification to {}: {}", user.getEmail(), e.getMessage());
        }
    }

    @Override
    @Async
    public void notify(User user, Notification notification) {
        try {
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setTo(user.getEmail());
            mailMessage.setSubject(notification.getTitle());
            mailMessage.setText(notification.getMessage());
            
            mailSender.send(mailMessage);
            log.info("Email notification sent to {}: {}", user.getEmail(), notification.getTitle());
        } catch (Exception e) {
            log.error("Failed to send email notification to {}: {}", user.getEmail(), e.getMessage());
        }
    }

    @Override
    public boolean supports(String notificationType) {
        return "EMAIL".equals(notificationType) || "BOTH".equals(notificationType);
    }

    private String buildEmailContent(User user, Reminder reminder, String message) {
        StringBuilder content = new StringBuilder();
        content.append("Hello ").append(user.getFirstName()).append(",\n\n");
        content.append(message).append("\n\n");
        content.append("Task: ").append(reminder.getTask().getTitle()).append("\n");
        
        if (reminder.getTask().getDescription() != null) {
            content.append("Description: ").append(reminder.getTask().getDescription()).append("\n");
        }
        if (reminder.getTask().getDueDate() != null) {
            content.append("Due Date: ").append(reminder.getTask().getDueDate());
            if (reminder.getTask().getDueTime() != null) {
                content.append(" at ").append(reminder.getTask().getDueTime());
            }
            content.append("\n");
        }
        content.append("Priority: ").append(reminder.getTask().getPriority().name()).append("\n\n");
        content.append("Best regards,\n");
        content.append("Smart Task Manager");
        
        return content.toString();
    }
}

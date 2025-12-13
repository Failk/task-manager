package com.taskmanager.observer;

import com.taskmanager.entity.Reminder;
import com.taskmanager.entity.Task;
import com.taskmanager.entity.User;
import com.taskmanager.enums.ReminderType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

/**
 * Observer Pattern Implementation: Email Notification Observer
 * Sends email notifications when events occur
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class EmailNotificationObserver implements NotificationObserver {
    
    private final JavaMailSender mailSender;
    
    @Override
    public void onReminderTriggered(Reminder reminder, Task task, User user) {
        if (reminder.getReminderType() != ReminderType.EMAIL) {
            return;
        }
        
        if (user.getNotificationPreferences() != null && 
            !user.getNotificationPreferences().isEmailEnabled()) {
            log.info("Email notifications disabled for user: {}", user.getEmail());
            return;
        }
        
        try {
            sendEmail(
                user.getEmail(),
                "Reminder: " + task.getTitle() + " due soon",
                buildReminderEmailBody(task, reminder)
            );
            reminder.send();
            log.info("Email reminder sent for task {} to {}", task.getId(), user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send email reminder for task {}: {}", task.getId(), e.getMessage());
        }
    }
    
    @Override
    public void onTaskOverdue(Task task, User user) {
        if (user.getNotificationPreferences() != null && 
            !user.getNotificationPreferences().isEmailEnabled()) {
            return;
        }
        
        try {
            sendEmail(
                user.getEmail(),
                "Overdue Task: " + task.getTitle(),
                buildOverdueEmailBody(task)
            );
            log.info("Overdue notification sent for task {} to {}", task.getId(), user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send overdue notification for task {}: {}", task.getId(), e.getMessage());
        }
    }
    
    @Override
    public void onTaskCompleted(Task task, User user) {
        // Optional: Send completion confirmation email
        log.info("Task {} completed by {}", task.getId(), user.getEmail());
    }
    
    @Override
    public String getObserverType() {
        return "EMAIL";
    }
    
    private void sendEmail(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        message.setFrom("noreply@taskmanager.com");
        
        // Note: In production, uncomment the line below
        // mailSender.send(message);
        log.info("Email would be sent to: {} with subject: {}", to, subject);
    }
    
    private String buildReminderEmailBody(Task task, Reminder reminder) {
        StringBuilder body = new StringBuilder();
        body.append("Hello,\n\n");
        body.append("This is a reminder for your upcoming task:\n\n");
        body.append("Task: ").append(task.getTitle()).append("\n");
        if (task.getDescription() != null && !task.getDescription().isEmpty()) {
            body.append("Description: ").append(task.getDescription()).append("\n");
        }
        body.append("Due Date: ").append(task.getDueDate()).append("\n");
        body.append("Priority: ").append(task.getPriority().getDescription()).append("\n\n");
        body.append("Click here to view in Task Manager: [Link]\n\n");
        body.append("Best regards,\n");
        body.append("Smart Task Manager");
        return body.toString();
    }
    
    private String buildOverdueEmailBody(Task task) {
        StringBuilder body = new StringBuilder();
        body.append("Hello,\n\n");
        body.append("The following task is overdue:\n\n");
        body.append("Task: ").append(task.getTitle()).append("\n");
        body.append("Was Due: ").append(task.getDueDate()).append("\n");
        body.append("Priority: ").append(task.getPriority().getDescription()).append("\n\n");
        body.append("Please complete or reschedule this task.\n\n");
        body.append("Best regards,\n");
        body.append("Smart Task Manager");
        return body.toString();
    }
}

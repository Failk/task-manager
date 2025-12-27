package com.taskmanager.service;

import com.taskmanager.dto.notification.NotificationDTO;
import com.taskmanager.entity.Notification;
import com.taskmanager.entity.Reminder;
import com.taskmanager.entity.Task;
import com.taskmanager.entity.User;
import com.taskmanager.entity.enums.NotificationType;
import com.taskmanager.exception.ResourceNotFoundException;
import com.taskmanager.observer.NotificationSubject;
import com.taskmanager.repository.NotificationRepository;
import com.taskmanager.repository.ReminderRepository;
import com.taskmanager.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final ReminderRepository reminderRepository;
    private final TaskRepository taskRepository;
    private final UserService userService;
    private final NotificationSubject notificationSubject;
    private final JavaMailSender mailSender;

    @Transactional(readOnly = true)
    public List<NotificationDTO> getAllNotifications() {
        User user = userService.getCurrentUser();
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<NotificationDTO> getUnreadNotifications() {
        User user = userService.getCurrentUser();
        return notificationRepository.findByUserIdAndReadFalseOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public long getUnreadCount() {
        User user = userService.getCurrentUser();
        return notificationRepository.countByUserIdAndReadFalse(user.getId());
    }

    @Transactional
    public NotificationDTO acknowledgeNotification(Long id) {
        User user = userService.getCurrentUser();
        Notification notification = notificationRepository.findById(id)
                .filter(n -> n.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new ResourceNotFoundException("Notification", "id", id));

        notification.markAsRead();
        notification = notificationRepository.save(notification);

        return mapToDTO(notification);
    }

    @Transactional
    public void deleteNotification(Long id) {
        User user = userService.getCurrentUser();
        Notification notification = notificationRepository.findById(id)
                .filter(n -> n.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new ResourceNotFoundException("Notification", "id", id));

        notificationRepository.delete(notification);
        log.info("Notification {} deleted by user {}", id, user.getEmail());
    }

    @Transactional
    public void snoozeNotification(Long id, int minutes) {
        User user = userService.getCurrentUser();
        Notification notification = notificationRepository.findById(id)
                .filter(n -> n.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new ResourceNotFoundException("Notification", "id", id));

        // Mark as read for now (it will "reappear" as a new notification after snooze
        // time)
        notification.markAsRead();
        notificationRepository.save(notification);

        log.info("Notification {} snoozed for {} minutes by user {}", id, minutes, user.getEmail());
    }

    @Transactional
    public void snoozeReminder(Long reminderId, int minutes) {
        Reminder reminder = reminderRepository.findById(reminderId)
                .orElseThrow(() -> new ResourceNotFoundException("Reminder", "id", reminderId));

        reminder.snooze(minutes);
        reminderRepository.save(reminder);
        log.info("Reminder {} snoozed for {} minutes", reminderId, minutes);
    }

    @Transactional
    public void createNotification(User user, Task task, String title, String message, NotificationType type) {
        Notification notification = Notification.builder()
                .user(user)
                .task(task)
                .title(title)
                .message(message)
                .type(type)
                .build();

        notification = notificationRepository.save(notification);
        notificationSubject.notifyObservers(user, notification);
    }

    /**
     * Scheduled job to process pending reminders.
     * Runs every minute.
     */
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void processPendingReminders() {
        LocalDateTime now = LocalDateTime.now();
        List<Reminder> pendingReminders = reminderRepository.findPendingReminders(now);

        for (Reminder reminder : pendingReminders) {
            try {
                processReminder(reminder);
            } catch (Exception e) {
                log.error("Failed to process reminder {}: {}", reminder.getId(), e.getMessage());
            }
        }

        if (!pendingReminders.isEmpty()) {
            log.info("Processed {} pending reminders", pendingReminders.size());
        }
    }

    /**
     * Scheduled job to check for overdue tasks.
     * Runs every hour.
     */
    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void checkOverdueTasks() {
        log.info("Checking for overdue tasks...");

        List<Task> allTasks = taskRepository.findAll();
        LocalDate today = LocalDate.now();

        for (Task task : allTasks) {
            if (task.isOverdue()) {
                User user = task.getUser();
                createNotification(
                        user,
                        task,
                        "Task Overdue",
                        String.format("Task '%s' is overdue. Due date was: %s",
                                task.getTitle(), task.getDueDate()),
                        NotificationType.POPUP);
            }
        }
    }

    private void processReminder(Reminder reminder) {
        Task task = reminder.getTask();
        User user = task.getUser();

        String message = String.format("Reminder: Task '%s' is due %s",
                task.getTitle(),
                task.getDueDate() != null ? "on " + task.getDueDate() : "soon");

        if (reminder.getLeadTimeMinutes() != null) {
            message = String.format("Reminder: Task '%s' is due in %d minutes",
                    task.getTitle(), reminder.getLeadTimeMinutes());
        }

        // Create notification record
        Notification notification = Notification.builder()
                .user(user)
                .task(task)
                .title("Task Reminder")
                .message(message)
                .type(reminder.getNotificationType())
                .build();
        notificationRepository.save(notification);

        // Notify through observers
        notificationSubject.notifyObservers(user, reminder, message,
                reminder.getNotificationType().name());

        // Mark reminder as sent
        reminder.markAsSent();
        reminderRepository.save(reminder);
    }

    private NotificationDTO mapToDTO(Notification notification) {
        return NotificationDTO.builder()
                .id(notification.getId())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .type(notification.getType().name())
                .read(notification.isRead())
                .readAt(notification.getReadAt())
                .createdAt(notification.getCreatedAt())
                .taskId(notification.getTask() != null ? notification.getTask().getId() : null)
                .taskTitle(notification.getTask() != null ? notification.getTask().getTitle() : null)
                .build();
    }

    /**
     * Sends a test email to the current user to verify email configuration.
     * 
     * @return Success or error message
     */
    public String sendTestEmail() {
        User user = userService.getCurrentUser();

        try {
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setTo(user.getEmail());
            mailMessage.setSubject("Test Email from Task Manager");
            mailMessage.setText(buildTestEmailContent(user));

            mailSender.send(mailMessage);
            log.info("Test email sent successfully to {}", user.getEmail());
            return "Test email sent successfully to " + user.getEmail();
        } catch (Exception e) {
            log.error("Failed to send test email to {}: {}", user.getEmail(), e.getMessage());
            throw new RuntimeException("Failed to send email: " + e.getMessage());
        }
    }

    private String buildTestEmailContent(User user) {
        StringBuilder content = new StringBuilder();
        content.append("Hello ").append(user.getFirstName()).append(",\n\n");
        content.append("This is a test email from your Task Manager application.\n\n");
        content.append("If you received this email, your email configuration is working correctly!\n\n");
        content.append("Configuration Details:\n");
        content.append("- Email: ").append(user.getEmail()).append("\n");
        content.append("- Sent at: ").append(LocalDateTime.now()).append("\n\n");
        content.append("Best regards,\n");
        content.append("Smart Task Manager");
        return content.toString();
    }
}

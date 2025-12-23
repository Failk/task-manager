package com.taskmanager.observer;

import com.taskmanager.dto.notification.NotificationDTO;
import com.taskmanager.entity.Notification;
import com.taskmanager.entity.Reminder;
import com.taskmanager.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PopupNotificationObserver implements NotificationObserver {

    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void notify(User user, Reminder reminder, String message) {
        NotificationDTO notificationDTO = NotificationDTO.builder()
                .title("Task Reminder")
                .message(message)
                .type("POPUP")
                .taskId(reminder.getTask().getId())
                .taskTitle(reminder.getTask().getTitle())
                .build();

        sendWebSocketNotification(user.getId(), notificationDTO);
        log.info("Popup notification sent to user {} for task: {}", 
                user.getId(), reminder.getTask().getTitle());
    }

    @Override
    public void notify(User user, Notification notification) {
        NotificationDTO notificationDTO = NotificationDTO.builder()
                .id(notification.getId())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .type(notification.getType().name())
                .read(notification.isRead())
                .createdAt(notification.getCreatedAt())
                .taskId(notification.getTask() != null ? notification.getTask().getId() : null)
                .taskTitle(notification.getTask() != null ? notification.getTask().getTitle() : null)
                .build();

        sendWebSocketNotification(user.getId(), notificationDTO);
        log.info("Popup notification sent to user {}: {}", user.getId(), notification.getTitle());
    }

    @Override
    public boolean supports(String notificationType) {
        return "POPUP".equals(notificationType) || "BOTH".equals(notificationType);
    }

    private void sendWebSocketNotification(Long userId, NotificationDTO notification) {
        String destination = "/queue/notifications";
        messagingTemplate.convertAndSendToUser(
                userId.toString(),
                destination,
                notification
        );
    }
}

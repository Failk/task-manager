package com.taskmanager.observer;

import com.taskmanager.entity.Reminder;
import com.taskmanager.entity.Task;
import com.taskmanager.entity.User;
import com.taskmanager.enums.ReminderType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Observer Pattern Implementation: Popup Notification Observer
 * Sends real-time popup notifications via WebSocket
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PopupNotificationObserver implements NotificationObserver {
    
    private final SimpMessagingTemplate messagingTemplate;
    
    @Override
    public void onReminderTriggered(Reminder reminder, Task task, User user) {
        if (reminder.getReminderType() != ReminderType.POPUP) {
            return;
        }
        
        if (user.getNotificationPreferences() != null && 
            !user.getNotificationPreferences().isPopupEnabled()) {
            log.info("Popup notifications disabled for user: {}", user.getUsername());
            return;
        }
        
        try {
            Map<String, Object> notification = new HashMap<>();
            notification.put("type", "REMINDER");
            notification.put("taskId", task.getId());
            notification.put("title", task.getTitle());
            notification.put("message", "Task due: " + task.getDueDate());
            notification.put("priority", task.getPriority().name());
            notification.put("dueDate", task.getDueDate().toString());
            
            sendWebSocketNotification(user.getId(), notification);
            reminder.send();
            log.info("Popup reminder sent for task {} to user {}", task.getId(), user.getId());
        } catch (Exception e) {
            log.error("Failed to send popup reminder for task {}: {}", task.getId(), e.getMessage());
        }
    }
    
    @Override
    public void onTaskOverdue(Task task, User user) {
        if (user.getNotificationPreferences() != null && 
            !user.getNotificationPreferences().isPopupEnabled()) {
            return;
        }
        
        try {
            Map<String, Object> notification = new HashMap<>();
            notification.put("type", "OVERDUE");
            notification.put("taskId", task.getId());
            notification.put("title", task.getTitle());
            notification.put("message", "This task is overdue!");
            notification.put("priority", task.getPriority().name());
            notification.put("dueDate", task.getDueDate().toString());
            
            sendWebSocketNotification(user.getId(), notification);
            log.info("Overdue popup sent for task {} to user {}", task.getId(), user.getId());
        } catch (Exception e) {
            log.error("Failed to send overdue popup for task {}: {}", task.getId(), e.getMessage());
        }
    }
    
    @Override
    public void onTaskCompleted(Task task, User user) {
        try {
            Map<String, Object> notification = new HashMap<>();
            notification.put("type", "COMPLETED");
            notification.put("taskId", task.getId());
            notification.put("title", task.getTitle());
            notification.put("message", "Task completed! Great job!");
            
            sendWebSocketNotification(user.getId(), notification);
            log.info("Completion popup sent for task {} to user {}", task.getId(), user.getId());
        } catch (Exception e) {
            log.error("Failed to send completion popup for task {}: {}", task.getId(), e.getMessage());
        }
    }
    
    @Override
    public String getObserverType() {
        return "POPUP";
    }
    
    private void sendWebSocketNotification(Long userId, Map<String, Object> notification) {
        String destination = "/user/" + userId + "/notifications";
        messagingTemplate.convertAndSend(destination, notification);
    }
}

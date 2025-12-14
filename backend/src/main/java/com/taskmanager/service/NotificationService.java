package com.taskmanager.service;

import com.taskmanager.controller.NotificationController.UpdatePreferencesRequest;
import com.taskmanager.entity.NotificationPreferences;
import com.taskmanager.entity.Reminder;
import com.taskmanager.entity.Task;
import com.taskmanager.entity.User;
import com.taskmanager.exception.ResourceNotFoundException;
import com.taskmanager.observer.NotificationObserver;
import com.taskmanager.observer.NotificationSubject;
import com.taskmanager.repository.ReminderRepository;
import com.taskmanager.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Notification Service implementing the Observer Pattern
 * Acts as the Subject that notifies observers when events occur
 */
@Service
@Slf4j
public class NotificationService implements NotificationSubject {

    private final ReminderRepository reminderRepository;
    private final UserRepository userRepository;
    private final List<NotificationObserver> observers = new ArrayList<>();

    public NotificationService(ReminderRepository reminderRepository,
                               UserRepository userRepository,
                               @Autowired(required = false) List<NotificationObserver> observers) {
        this.reminderRepository = reminderRepository;
        this.userRepository = userRepository;
        if (observers != null) {
            this.observers.addAll(observers);
        }
    }
    
    @Override
    public void registerObserver(NotificationObserver observer) {
        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }
    
    @Override
    public void removeObserver(NotificationObserver observer) {
        observers.remove(observer);
    }
    
    @Override
    public void notifyObservers() {
        // This is called by the scheduled job
    }
    
    /**
     * Scheduled job to process pending reminders
     * Runs every minute
     */
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void processReminders() {
        log.debug("Processing pending reminders...");
        
        LocalDateTime now = LocalDateTime.now();
        List<Reminder> pendingReminders = reminderRepository.findPendingReminders(now);
        
        for (Reminder reminder : pendingReminders) {
            if (reminder.shouldSend()) {
                Task task = reminder.getTask();
                User user = task.getProject().getUser();
                
                // Notify all observers
                for (NotificationObserver observer : observers) {
                    try {
                        observer.onReminderTriggered(reminder, task, user);
                    } catch (Exception e) {
                        log.error("Error notifying observer {}: {}", observer.getObserverType(), e.getMessage());
                    }
                }
                
                reminder.send();
                reminderRepository.save(reminder);
            }
        }
        
        log.debug("Processed {} reminders", pendingReminders.size());
    }
    
    /**
     * Scheduled job to check for overdue tasks
     * Runs every hour
     */
    @Scheduled(fixedRate = 3600000)
    @Transactional
    public void checkOverdueTasks() {
        log.debug("Checking for overdue tasks...");
        
        // This would need to be implemented with a query that gets users with overdue tasks
        // For now, this is a placeholder
    }
    
    /**
     * Snooze a reminder for a specified duration
     */
    @Transactional
    public Reminder snoozeReminder(Long reminderId, Long userId, int minutes) {
        Reminder reminder = reminderRepository.findById(reminderId)
                .orElseThrow(() -> new ResourceNotFoundException("Reminder not found"));
        
        // Verify ownership
        if (!reminder.getTask().getProject().getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Reminder not found");
        }
        
        reminder.snooze(Duration.ofMinutes(minutes));
        return reminderRepository.save(reminder);
    }
    
    /**
     * Acknowledge a reminder
     */
    @Transactional
    public void acknowledgeReminder(Long reminderId, Long userId) {
        Reminder reminder = reminderRepository.findById(reminderId)
                .orElseThrow(() -> new ResourceNotFoundException("Reminder not found"));
        
        // Verify ownership
        if (!reminder.getTask().getProject().getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Reminder not found");
        }
        
        reminder.acknowledge();
        reminderRepository.save(reminder);
    }
    
    /**
     * Get notification preferences for a user
     */
    @Transactional(readOnly = true)
    public NotificationPreferences getPreferences(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        NotificationPreferences prefs = user.getNotificationPreferences();
        if (prefs == null) {
            prefs = NotificationPreferences.builder()
                    .user(user)
                    .emailEnabled(true)
                    .popupEnabled(true)
                    .dailyDigestEnabled(false)
                    .build();
        }
        return prefs;
    }
    
    /**
     * Update notification preferences for a user
     */
    @Transactional
    public NotificationPreferences updatePreferences(Long userId, UpdatePreferencesRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        NotificationPreferences prefs = user.getNotificationPreferences();
        if (prefs == null) {
            prefs = NotificationPreferences.builder()
                    .user(user)
                    .build();
        }
        
        if (request.getEmailNotificationsEnabled() != null) {
            prefs.setEmailEnabled(request.getEmailNotificationsEnabled());
        }
        if (request.getPopupNotificationsEnabled() != null) {
            prefs.setPopupEnabled(request.getPopupNotificationsEnabled());
        }
        if (request.getDailyDigestEnabled() != null) {
            prefs.setDailyDigestEnabled(request.getDailyDigestEnabled());
        }
        if (request.getDailyDigestTime() != null) {
            prefs.setDailyDigestTime(request.getDailyDigestTime());
        }
        if (request.getQuietHoursStart() != null) {
            prefs.setQuietHoursStart(request.getQuietHoursStart());
        }
        if (request.getQuietHoursEnd() != null) {
            prefs.setQuietHoursEnd(request.getQuietHoursEnd());
        }
        
        user.setNotificationPreferences(prefs);
        userRepository.save(user);
        return prefs;
    }
    
    /**
     * Get notification history for a user
     */
    public List<Reminder> getNotificationHistory(Long userId) {
        return reminderRepository.findNotificationHistory(userId);
    }
    
    /**
     * Trigger notification for task completion
     */
    public void notifyTaskCompleted(Task task, User user) {
        for (NotificationObserver observer : observers) {
            try {
                observer.onTaskCompleted(task, user);
            } catch (Exception e) {
                log.error("Error notifying observer {}: {}", observer.getObserverType(), e.getMessage());
            }
        }
    }
}

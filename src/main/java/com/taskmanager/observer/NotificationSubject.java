package com.taskmanager.observer;

import com.taskmanager.entity.Notification;
import com.taskmanager.entity.Reminder;
import com.taskmanager.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class NotificationSubject {

    private final List<NotificationObserver> observers;

    public NotificationSubject(List<NotificationObserver> observers) {
        this.observers = new ArrayList<>(observers);
    }

    public void addObserver(NotificationObserver observer) {
        observers.add(observer);
    }

    public void removeObserver(NotificationObserver observer) {
        observers.remove(observer);
    }

    public void notifyObservers(User user, Reminder reminder, String message, String notificationType) {
        log.debug("Notifying observers for reminder type: {}", notificationType);
        for (NotificationObserver observer : observers) {
            if (observer.supports(notificationType)) {
                try {
                    observer.notify(user, reminder, message);
                } catch (Exception e) {
                    log.error("Observer failed to process notification: {}", e.getMessage());
                }
            }
        }
    }

    public void notifyObservers(User user, Notification notification) {
        String notificationType = notification.getType().name();
        log.debug("Notifying observers for notification type: {}", notificationType);
        for (NotificationObserver observer : observers) {
            if (observer.supports(notificationType)) {
                try {
                    observer.notify(user, notification);
                } catch (Exception e) {
                    log.error("Observer failed to process notification: {}", e.getMessage());
                }
            }
        }
    }
}

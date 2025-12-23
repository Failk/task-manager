package com.taskmanager.observer;

import com.taskmanager.entity.Notification;
import com.taskmanager.entity.Reminder;
import com.taskmanager.entity.User;

public interface NotificationObserver {
    
    void notify(User user, Reminder reminder, String message);
    
    void notify(User user, Notification notification);
    
    boolean supports(String notificationType);
}

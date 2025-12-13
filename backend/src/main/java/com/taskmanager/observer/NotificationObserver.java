package com.taskmanager.observer;

import com.taskmanager.entity.Reminder;
import com.taskmanager.entity.Task;
import com.taskmanager.entity.User;

/**
 * Observer Pattern: Notification Observer Interface
 * Observers implement this to receive notification events
 */
public interface NotificationObserver {
    
    /**
     * Called when a reminder needs to be sent
     */
    void onReminderTriggered(Reminder reminder, Task task, User user);
    
    /**
     * Called when a task becomes overdue
     */
    void onTaskOverdue(Task task, User user);
    
    /**
     * Called when a task is completed
     */
    void onTaskCompleted(Task task, User user);
    
    /**
     * Get the type of notification this observer handles
     */
    String getObserverType();
}

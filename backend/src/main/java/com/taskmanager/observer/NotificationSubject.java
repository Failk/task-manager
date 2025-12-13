package com.taskmanager.observer;

/**
 * Observer Pattern: Subject Interface
 * The subject maintains a list of observers and notifies them of events
 */
public interface NotificationSubject {
    
    void registerObserver(NotificationObserver observer);
    
    void removeObserver(NotificationObserver observer);
    
    void notifyObservers();
}

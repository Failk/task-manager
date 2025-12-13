package com.taskmanager.enums;

public enum Priority {
    A("Critical - Urgent and Important"),
    B("Important - Scheduled Deadlines"),
    C("Complete When Possible"),
    D("Delegate or Defer");
    
    private final String description;
    
    Priority(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}

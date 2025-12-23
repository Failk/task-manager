package com.taskmanager.entity.enums;

public enum Priority {
    A("Critical - Must be done today"),
    B("Important - Should be done soon"),
    C("Nice to have - Can wait"),
    D("Delegate or Defer");

    private final String description;

    Priority(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}

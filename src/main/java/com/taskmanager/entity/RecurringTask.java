package com.taskmanager.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Entity
@DiscriminatorValue("RECURRING")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class RecurringTask extends Task {

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "recurrence_pattern_id")
    private RecurrencePattern recurrencePattern;

    @OneToMany(mappedBy = "recurringTask", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TaskInstance> taskInstances = new ArrayList<>();

    @Override
    public String getTaskType() {
        return "RECURRING";
    }

    public void addTaskInstance(TaskInstance instance) {
        taskInstances.add(instance);
        instance.setRecurringTask(this);
    }

    public void removeTaskInstance(TaskInstance instance) {
        taskInstances.remove(instance);
        instance.setRecurringTask(null);
    }
}

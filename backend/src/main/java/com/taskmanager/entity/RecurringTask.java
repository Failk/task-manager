package com.taskmanager.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@DiscriminatorValue("RECURRING")
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true, exclude = {"recurrencePattern", "instances"})
@SuperBuilder
public class RecurringTask extends Task {
    
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "recurrence_pattern_id")
    private RecurrencePattern recurrencePattern;
    
    private LocalDate startDate;
    private LocalDate endDate;
    
    @OneToMany(mappedBy = "recurringTask", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TaskInstance> instances = new ArrayList<>();
    
    @Override
    public String getTaskType() {
        return "RECURRING";
    }
    
    public List<TaskInstance> generateInstances(int count) {
        List<TaskInstance> newInstances = new ArrayList<>();
        if (recurrencePattern == null || count <= 0) {
            return newInstances;
        }
        
        LocalDateTime currentDate = getDueDate();
        int generated = 0;
        int occurrenceNumber = instances.size();
        
        while (generated < count && recurrencePattern.shouldContinue(occurrenceNumber, currentDate.toLocalDate())) {
            TaskInstance instance = TaskInstance.builder()
                    .recurringTask(this)
                    .instanceDate(currentDate.toLocalDate())
                    .instanceTime(currentDate.toLocalTime())
                    .overridden(false)
                    .status(getStatus())
                    .build();
            
            instances.add(instance);
            newInstances.add(instance);
            
            currentDate = recurrencePattern.calculateNextOccurrence(currentDate);
            generated++;
            occurrenceNumber++;
            
            // Safety check for end date
            if (endDate != null && currentDate.toLocalDate().isAfter(endDate)) {
                break;
            }
        }
        
        return newInstances;
    }
    
    public void skipOccurrence(LocalDate date) {
        instances.stream()
                .filter(i -> i.getInstanceDate().equals(date))
                .findFirst()
                .ifPresent(i -> i.setSkipped(true));
    }
    
    public TaskInstance modifyInstance(LocalDate date, LocalDateTime newDateTime) {
        return instances.stream()
                .filter(i -> i.getInstanceDate().equals(date))
                .findFirst()
                .map(instance -> {
                    instance.setOverridden(true);
                    instance.setInstanceDate(newDateTime.toLocalDate());
                    instance.setInstanceTime(newDateTime.toLocalTime());
                    return instance;
                })
                .orElse(null);
    }
}

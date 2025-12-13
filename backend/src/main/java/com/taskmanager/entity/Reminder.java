package com.taskmanager.entity;

import com.taskmanager.enums.ReminderType;
import jakarta.persistence.*;
import lombok.*;

import java.time.Duration;
import java.time.LocalDateTime;

@Entity
@Table(name = "reminders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reminder {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReminderType reminderType;
    
    @Column(nullable = false)
    private Long leadTimeMinutes; // Minutes before due date
    
    private LocalDateTime sentAt;
    
    @Column(nullable = false)
    @Builder.Default
    private boolean acknowledged = false;
    
    @Column(nullable = false)
    @Builder.Default
    private boolean sent = false;
    
    private LocalDateTime snoozedUntil;
    
    public Duration getLeadTime() {
        return Duration.ofMinutes(leadTimeMinutes);
    }
    
    public void setLeadTime(Duration duration) {
        this.leadTimeMinutes = duration.toMinutes();
    }
    
    public LocalDateTime getReminderTime() {
        if (task == null || task.getDueDate() == null) {
            return null;
        }
        return task.getDueDate().minusMinutes(leadTimeMinutes);
    }
    
    public boolean shouldSend() {
        if (sent || acknowledged) {
            return false;
        }
        LocalDateTime reminderTime = getReminderTime();
        if (reminderTime == null) {
            return false;
        }
        LocalDateTime now = LocalDateTime.now();
        if (snoozedUntil != null && now.isBefore(snoozedUntil)) {
            return false;
        }
        return !now.isBefore(reminderTime);
    }
    
    public void send() {
        this.sent = true;
        this.sentAt = LocalDateTime.now();
    }
    
    public void acknowledge() {
        this.acknowledged = true;
    }
    
    public void snooze(Duration duration) {
        this.snoozedUntil = LocalDateTime.now().plus(duration);
        this.sent = false; // Allow re-sending after snooze
    }
}

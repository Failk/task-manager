package com.taskmanager.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Duration;
import java.time.LocalTime;

@Entity
@Table(name = "notification_preferences")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationPreferences {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(nullable = false)
    @Builder.Default
    private boolean emailEnabled = true;
    
    @Column(nullable = false)
    @Builder.Default
    private boolean popupEnabled = true;

    @Column(nullable = false)
    @Builder.Default
    private boolean dailyDigestEnabled = false;

    @Column
    private LocalTime dailyDigestTime;

    @Column(nullable = false)
    @Builder.Default
    private Long defaultLeadTimeMinutes = 60L; // Default 1 hour

    private LocalTime quietHoursStart;
    private LocalTime quietHoursEnd;
    
    public Duration getDefaultLeadTime() {
        return Duration.ofMinutes(defaultLeadTimeMinutes);
    }
    
    public void setDefaultLeadTime(Duration duration) {
        this.defaultLeadTimeMinutes = duration.toMinutes();
    }
    
    public boolean isInQuietHours() {
        if (quietHoursStart == null || quietHoursEnd == null) {
            return false;
        }
        LocalTime now = LocalTime.now();
        if (quietHoursStart.isBefore(quietHoursEnd)) {
            return !now.isBefore(quietHoursStart) && !now.isAfter(quietHoursEnd);
        } else {
            // Quiet hours span midnight
            return !now.isBefore(quietHoursStart) || !now.isAfter(quietHoursEnd);
        }
    }

    // Helper methods for controller compatibility
    public boolean isEmailNotificationsEnabled() {
        return emailEnabled;
    }

    public boolean isPopupNotificationsEnabled() {
        return popupEnabled;
    }

    public boolean isDailyDigestEnabled() {
        return dailyDigestEnabled;
    }

    public LocalTime getDailyDigestTime() {
        return dailyDigestTime;
    }

    public boolean isQuietHoursEnabled() {
        return quietHoursStart != null && quietHoursEnd != null;
    }
}

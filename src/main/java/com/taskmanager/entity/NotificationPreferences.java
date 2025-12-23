package com.taskmanager.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;

@Entity
@Table(name = "notification_preferences")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationPreferences {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "email_enabled", nullable = false)
    @Builder.Default
    private boolean emailEnabled = true;

    @Column(name = "popup_enabled", nullable = false)
    @Builder.Default
    private boolean popupEnabled = true;

    @Column(name = "daily_digest_enabled", nullable = false)
    @Builder.Default
    private boolean dailyDigestEnabled = false;

    @Column(name = "daily_digest_time")
    private LocalTime dailyDigestTime;

    @Column(name = "reminder_lead_time_minutes", nullable = false)
    @Builder.Default
    private int reminderLeadTimeMinutes = 30;

    @Column(name = "overdue_notifications_enabled", nullable = false)
    @Builder.Default
    private boolean overdueNotificationsEnabled = true;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}

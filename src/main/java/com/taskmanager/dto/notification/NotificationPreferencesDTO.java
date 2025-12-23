package com.taskmanager.dto.notification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationPreferencesDTO {

    private boolean emailEnabled;
    private boolean popupEnabled;
    private boolean dailyDigestEnabled;
    private LocalTime dailyDigestTime;
    private int reminderLeadTimeMinutes;
    private boolean overdueNotificationsEnabled;
}

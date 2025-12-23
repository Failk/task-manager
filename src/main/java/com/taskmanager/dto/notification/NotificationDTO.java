package com.taskmanager.dto.notification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDTO {

    private Long id;
    private String title;
    private String message;
    private String type;
    private boolean read;
    private LocalDateTime readAt;
    private LocalDateTime createdAt;
    private Long taskId;
    private String taskTitle;
}

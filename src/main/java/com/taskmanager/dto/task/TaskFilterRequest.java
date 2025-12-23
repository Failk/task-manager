package com.taskmanager.dto.task;

import com.taskmanager.entity.enums.Priority;
import com.taskmanager.entity.enums.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskFilterRequest {

    private String keyword;
    private List<Priority> priorities;
    private List<TaskStatus> statuses;
    private List<Long> contextIds;
    private Long projectId;
    private LocalDate dueDateFrom;
    private LocalDate dueDateTo;
    private Boolean overdue;
    private String sortBy; // dueDate, priority, createdAt, title
    private String sortDirection; // ASC, DESC
}

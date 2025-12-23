package com.taskmanager.dto.context;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContextDTO {

    private Long id;
    private String name;
    private String description;
    private boolean isDefault;
    private int taskCount;
}

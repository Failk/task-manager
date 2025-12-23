package com.taskmanager.dto.context;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateContextRequest {

    @NotBlank(message = "Context name is required")
    @Size(max = 50, message = "Context name must be at most 50 characters")
    private String name;

    @Size(max = 200, message = "Description must be at most 200 characters")
    private String description;
}

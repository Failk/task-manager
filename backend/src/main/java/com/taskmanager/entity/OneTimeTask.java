package com.taskmanager.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@DiscriminatorValue("ONE_TIME")
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
public class OneTimeTask extends Task {
    
    @Override
    public String getTaskType() {
        return "ONE_TIME";
    }
}

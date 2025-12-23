package com.taskmanager.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@DiscriminatorValue("ONE_TIME")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class OneTimeTask extends Task {

    @Override
    public String getTaskType() {
        return "ONE_TIME";
    }
}

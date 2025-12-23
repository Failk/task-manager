package com.taskmanager.entity;

import com.taskmanager.entity.enums.RecurrenceFrequency;
import jakarta.persistence.*;
import lombok.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Set;

@Entity
@Table(name = "recurrence_patterns")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecurrencePattern {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RecurrenceFrequency frequency;

    @Column(nullable = false)
    @Builder.Default
    private int interval = 1; // e.g., every 2 weeks

    @ElementCollection
    @CollectionTable(name = "recurrence_days", joinColumns = @JoinColumn(name = "pattern_id"))
    @Column(name = "day_of_week")
    @Enumerated(EnumType.STRING)
    private Set<DayOfWeek> daysOfWeek; // for weekly recurrence

    @Column(name = "day_of_month")
    private Integer dayOfMonth; // for monthly recurrence

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "occurrences")
    private Integer occurrences; // number of times to repeat

    @OneToOne(mappedBy = "recurrencePattern")
    private RecurringTask recurringTask;
}

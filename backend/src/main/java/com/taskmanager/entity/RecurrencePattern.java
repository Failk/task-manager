package com.taskmanager.entity;

import com.taskmanager.enums.EndCondition;
import com.taskmanager.enums.Frequency;
import jakarta.persistence.*;
import lombok.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.Set;

@Entity
@Table(name = "recurrence_patterns")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecurrencePattern {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Frequency frequency;
    
    @Column(nullable = false)
    @Builder.Default
    private Integer interval = 1; // e.g., every 2 weeks
    
    @ElementCollection
    @CollectionTable(name = "recurrence_days_of_week", joinColumns = @JoinColumn(name = "pattern_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week")
    private Set<DayOfWeek> daysOfWeek;
    
    private Integer dayOfMonth; // For monthly recurrence
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private EndCondition endCondition = EndCondition.NEVER;
    
    private Integer occurrenceCount; // For AFTER_OCCURRENCES
    
    private LocalDate endDate; // For BY_DATE
    
    public LocalDateTime calculateNextOccurrence(LocalDateTime from) {
        switch (frequency) {
            case DAILY:
                return from.plusDays(interval);
            case WEEKLY:
                if (daysOfWeek == null || daysOfWeek.isEmpty()) {
                    return from.plusWeeks(interval);
                }
                // Find next day of week
                LocalDateTime next = from.plusDays(1);
                while (!daysOfWeek.contains(next.getDayOfWeek())) {
                    next = next.plusDays(1);
                    // If we've cycled through a week, add the interval
                    if (next.getDayOfWeek() == from.getDayOfWeek()) {
                        next = next.plusWeeks(interval - 1);
                    }
                }
                return next;
            case MONTHLY:
                LocalDateTime nextMonth = from.plusMonths(interval);
                if (dayOfMonth != null) {
                    int maxDay = nextMonth.toLocalDate().lengthOfMonth();
                    int targetDay = Math.min(dayOfMonth, maxDay);
                    nextMonth = nextMonth.withDayOfMonth(targetDay);
                }
                return nextMonth;
            case CUSTOM:
                // Custom handling based on daysOfWeek and interval
                return from.plusDays(interval);
            default:
                return from.plusDays(1);
        }
    }
    
    public boolean shouldContinue(int currentOccurrence, LocalDate currentDate) {
        switch (endCondition) {
            case NEVER:
                return true;
            case AFTER_OCCURRENCES:
                return occurrenceCount == null || currentOccurrence < occurrenceCount;
            case BY_DATE:
                return endDate == null || !currentDate.isAfter(endDate);
            default:
                return true;
        }
    }
    
    public boolean validatePattern() {
        if (frequency == null) return false;
        if (interval == null || interval < 1) return false;
        if (frequency == Frequency.WEEKLY && (daysOfWeek == null || daysOfWeek.isEmpty())) {
            // Default to current day if not specified
            return true;
        }
        if (frequency == Frequency.MONTHLY && dayOfMonth != null && (dayOfMonth < 1 || dayOfMonth > 31)) {
            return false;
        }
        if (endCondition == EndCondition.AFTER_OCCURRENCES && (occurrenceCount == null || occurrenceCount < 1)) {
            return false;
        }
        if (endCondition == EndCondition.BY_DATE && endDate == null) {
            return false;
        }
        return true;
    }
}

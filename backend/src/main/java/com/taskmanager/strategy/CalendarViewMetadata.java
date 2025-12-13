package com.taskmanager.strategy;

import lombok.*;

import java.time.LocalDate;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CalendarViewMetadata {
    private String viewType;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private LocalDate today;
    private String periodLabel; // e.g., "December 2025", "Dec 8-14, 2025"
    private Map<LocalDate, Integer> taskCountPerDay;
}

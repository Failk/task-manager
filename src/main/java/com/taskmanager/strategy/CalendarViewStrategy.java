package com.taskmanager.strategy;

import com.taskmanager.dto.calendar.CalendarViewDTO;

import java.time.LocalDate;

public interface CalendarViewStrategy {
    
    CalendarViewDTO generateView(Long userId, LocalDate referenceDate);
    
    String getViewType();
}

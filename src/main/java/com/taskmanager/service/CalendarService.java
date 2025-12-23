package com.taskmanager.service;

import com.taskmanager.dto.calendar.CalendarViewDTO;
import com.taskmanager.entity.User;
import com.taskmanager.strategy.CalendarViewStrategy;
import com.taskmanager.strategy.DailyCalendarView;
import com.taskmanager.strategy.MonthlyCalendarView;
import com.taskmanager.strategy.WeeklyCalendarView;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
public class CalendarService {

    private final DailyCalendarView dailyCalendarView;
    private final WeeklyCalendarView weeklyCalendarView;
    private final MonthlyCalendarView monthlyCalendarView;
    private final UserService userService;

    public CalendarViewDTO getDailyView(LocalDate date) {
        User user = userService.getCurrentUser();
        return executeStrategy(dailyCalendarView, user.getId(), date);
    }

    public CalendarViewDTO getWeeklyView(LocalDate date) {
        User user = userService.getCurrentUser();
        return executeStrategy(weeklyCalendarView, user.getId(), date);
    }

    public CalendarViewDTO getMonthlyView(LocalDate date) {
        User user = userService.getCurrentUser();
        return executeStrategy(monthlyCalendarView, user.getId(), date);
    }

    private CalendarViewDTO executeStrategy(CalendarViewStrategy strategy, Long userId, LocalDate date) {
        log.debug("Generating {} view for user {} with reference date {}", 
                strategy.getViewType(), userId, date);
        return strategy.generateView(userId, date);
    }
}

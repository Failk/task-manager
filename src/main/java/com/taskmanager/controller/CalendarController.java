package com.taskmanager.controller;

import com.taskmanager.dto.calendar.CalendarViewDTO;
import com.taskmanager.dto.common.ApiResponse;
import com.taskmanager.service.CalendarService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/calendar")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Calendar", description = "Calendar view operations using Strategy Pattern")
public class CalendarController {

    private final CalendarService calendarService;

    @Operation(summary = "Get daily calendar view using DailyCalendarView strategy")
    @GetMapping("/daily")
    public ResponseEntity<ApiResponse<CalendarViewDTO>> getDailyView(
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        CalendarViewDTO view = calendarService.getDailyView(date);
        return ResponseEntity.ok(ApiResponse.success(view));
    }

    @Operation(summary = "Get weekly calendar view using WeeklyCalendarView strategy")
    @GetMapping("/weekly")
    public ResponseEntity<ApiResponse<CalendarViewDTO>> getWeeklyView(
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        CalendarViewDTO view = calendarService.getWeeklyView(date);
        return ResponseEntity.ok(ApiResponse.success(view));
    }

    @Operation(summary = "Get monthly calendar view using MonthlyCalendarView strategy")
    @GetMapping("/monthly")
    public ResponseEntity<ApiResponse<CalendarViewDTO>> getMonthlyView(
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        CalendarViewDTO view = calendarService.getMonthlyView(date);
        return ResponseEntity.ok(ApiResponse.success(view));
    }
}

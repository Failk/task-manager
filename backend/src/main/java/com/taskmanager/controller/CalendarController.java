package com.taskmanager.controller;

import com.taskmanager.entity.User;
import com.taskmanager.service.CalendarService;
import com.taskmanager.strategy.CalendarViewMetadata;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/calendar")
@RequiredArgsConstructor
@Tag(name = "Calendar", description = "Calendar view endpoints")
@SecurityRequirement(name = "bearerAuth")
public class CalendarController {
    
    private final CalendarService calendarService;
    
    @GetMapping("/daily")
    @Operation(summary = "Get daily calendar view")
    public ResponseEntity<CalendarViewResponse> getDailyView(
            @AuthenticationPrincipal User user,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        var result = calendarService.getCalendarView(user.getId(), date, "daily");
        return ResponseEntity.ok(new CalendarViewResponse(result.getKey(), result.getValue()));
    }
    
    @GetMapping("/weekly")
    @Operation(summary = "Get weekly calendar view")
    public ResponseEntity<CalendarViewResponse> getWeeklyView(
            @AuthenticationPrincipal User user,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        var result = calendarService.getCalendarView(user.getId(), date, "weekly");
        return ResponseEntity.ok(new CalendarViewResponse(result.getKey(), result.getValue()));
    }
    
    @GetMapping("/monthly")
    @Operation(summary = "Get monthly calendar view")
    public ResponseEntity<CalendarViewResponse> getMonthlyView(
            @AuthenticationPrincipal User user,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        var result = calendarService.getCalendarView(user.getId(), date, "monthly");
        return ResponseEntity.ok(new CalendarViewResponse(result.getKey(), result.getValue()));
    }
    
    public record CalendarViewResponse(
            CalendarViewMetadata metadata,
            Map<LocalDate, ?> tasks
    ) {}
}

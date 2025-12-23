package com.taskmanager.controller;

import com.taskmanager.dto.common.ApiResponse;
import com.taskmanager.dto.context.ContextDTO;
import com.taskmanager.dto.context.CreateContextRequest;
import com.taskmanager.service.ContextService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/contexts")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Contexts", description = "Context tag management")
public class ContextController {

    private final ContextService contextService;

    @Operation(summary = "Get all context tags")
    @GetMapping
    public ResponseEntity<ApiResponse<List<ContextDTO>>> getAllContexts() {
        List<ContextDTO> contexts = contextService.getAllContexts();
        return ResponseEntity.ok(ApiResponse.success(contexts));
    }

    @Operation(summary = "Get default context tags")
    @GetMapping("/default")
    public ResponseEntity<ApiResponse<List<ContextDTO>>> getDefaultContexts() {
        List<ContextDTO> contexts = contextService.getDefaultContexts();
        return ResponseEntity.ok(ApiResponse.success(contexts));
    }

    @Operation(summary = "Create custom context tag")
    @PostMapping
    public ResponseEntity<ApiResponse<ContextDTO>> createContext(
            @Valid @RequestBody CreateContextRequest request) {
        ContextDTO context = contextService.createContext(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Context created successfully", context));
    }

    @Operation(summary = "Delete custom context tag")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteContext(@PathVariable Long id) {
        contextService.deleteContext(id);
        return ResponseEntity.ok(ApiResponse.success("Context deleted successfully", null));
    }
}

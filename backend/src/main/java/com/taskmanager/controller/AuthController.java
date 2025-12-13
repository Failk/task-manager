package com.taskmanager.controller;

import com.taskmanager.dto.UserDto;
import com.taskmanager.entity.User;
import com.taskmanager.security.JwtService;
import com.taskmanager.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "User authentication endpoints")
public class AuthController {
    
    private final UserService userService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    
    @PostMapping("/register")
    @Operation(summary = "Register a new user")
    public ResponseEntity<UserDto.AuthResponse> register(@Valid @RequestBody UserDto.RegisterRequest request) {
        User user = userService.register(request);
        String token = jwtService.generateToken(user, user.getId());
        
        return ResponseEntity.ok(UserDto.AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresIn(jwtService.getExpirationTime())
                .user(userService.toResponse(user))
                .build());
    }
    
    @PostMapping("/login")
    @Operation(summary = "Authenticate user and get JWT token")
    public ResponseEntity<UserDto.AuthResponse> login(@Valid @RequestBody UserDto.LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        
        User user = (User) authentication.getPrincipal();
        userService.updateLastLogin(user.getId());
        String token = jwtService.generateToken(user, user.getId());
        
        return ResponseEntity.ok(UserDto.AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresIn(jwtService.getExpirationTime())
                .user(userService.toResponse(user))
                .build());
    }
}

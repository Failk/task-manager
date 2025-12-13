package com.taskmanager.service;

import com.taskmanager.dto.UserDto;
import com.taskmanager.entity.NotificationPreferences;
import com.taskmanager.entity.User;
import com.taskmanager.exception.ResourceNotFoundException;
import com.taskmanager.exception.ValidationException;
import com.taskmanager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Transactional
    public User register(UserDto.RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ValidationException("Email already registered");
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ValidationException("Username already taken");
        }
        
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .enabled(true)
                .build();
        
        // Create default notification preferences
        NotificationPreferences preferences = NotificationPreferences.builder()
                .user(user)
                .emailEnabled(true)
                .popupEnabled(true)
                .defaultLeadTimeMinutes(60L)
                .build();
        
        user.setNotificationPreferences(preferences);
        
        return userRepository.save(user);
    }
    
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
    
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
    
    @Transactional
    public User updateProfile(Long userId, UserDto.UpdateProfileRequest request) {
        User user = findById(userId);
        
        if (request.getUsername() != null && !request.getUsername().equals(user.getUsername())) {
            if (userRepository.existsByUsername(request.getUsername())) {
                throw new ValidationException("Username already taken");
            }
            user.setUsername(request.getUsername());
        }
        
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new ValidationException("Email already registered");
            }
            user.setEmail(request.getEmail());
        }
        
        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }
        
        return userRepository.save(user);
    }
    
    @Transactional
    public void changePassword(Long userId, UserDto.ChangePasswordRequest request) {
        User user = findById(userId);
        
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new ValidationException("Current password is incorrect");
        }
        
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }
    
    @Transactional
    public void updateLastLogin(Long userId) {
        User user = findById(userId);
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);
    }
    
    public UserDto.UserResponse toResponse(User user) {
        return UserDto.UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .createdAt(user.getCreatedAt())
                .lastLogin(user.getLastLogin())
                .build();
    }
}

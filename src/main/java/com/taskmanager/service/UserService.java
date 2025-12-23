package com.taskmanager.service;

import com.taskmanager.dto.notification.NotificationPreferencesDTO;
import com.taskmanager.dto.user.ChangePasswordRequest;
import com.taskmanager.dto.user.UpdateUserRequest;
import com.taskmanager.dto.user.UserDTO;
import com.taskmanager.entity.NotificationPreferences;
import com.taskmanager.entity.User;
import com.taskmanager.exception.ResourceNotFoundException;
import com.taskmanager.exception.ValidationException;
import com.taskmanager.repository.NotificationPreferencesRepository;
import com.taskmanager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final NotificationPreferencesRepository notificationPreferencesRepository;
    private final PasswordEncoder passwordEncoder;

    public User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    public UserDTO getCurrentUserDTO() {
        User user = getCurrentUser();
        return mapToDTO(user);
    }

    @Transactional
    public UserDTO updateUser(UpdateUserRequest request) {
        User user = getCurrentUser();

        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }

        user = userRepository.save(user);
        log.info("User updated: {}", user.getEmail());

        return mapToDTO(user);
    }

    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        User user = getCurrentUser();

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new ValidationException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        log.info("Password changed for user: {}", user.getEmail());
    }

    public NotificationPreferencesDTO getNotificationPreferences() {
        User user = getCurrentUser();
        NotificationPreferences prefs = notificationPreferencesRepository.findByUserId(user.getId())
                .orElseGet(() -> createDefaultNotificationPreferences(user));
        return mapToDTO(prefs);
    }

    @Transactional
    public NotificationPreferencesDTO updateNotificationPreferences(NotificationPreferencesDTO request) {
        User user = getCurrentUser();
        NotificationPreferences prefs = notificationPreferencesRepository.findByUserId(user.getId())
                .orElseGet(() -> createDefaultNotificationPreferences(user));

        prefs.setEmailEnabled(request.isEmailEnabled());
        prefs.setPopupEnabled(request.isPopupEnabled());
        prefs.setDailyDigestEnabled(request.isDailyDigestEnabled());
        prefs.setDailyDigestTime(request.getDailyDigestTime());
        prefs.setReminderLeadTimeMinutes(request.getReminderLeadTimeMinutes());
        prefs.setOverdueNotificationsEnabled(request.isOverdueNotificationsEnabled());

        prefs = notificationPreferencesRepository.save(prefs);
        log.info("Notification preferences updated for user: {}", user.getEmail());

        return mapToDTO(prefs);
    }

    private NotificationPreferences createDefaultNotificationPreferences(User user) {
        NotificationPreferences prefs = NotificationPreferences.builder()
                .user(user)
                .emailEnabled(true)
                .popupEnabled(true)
                .dailyDigestEnabled(false)
                .reminderLeadTimeMinutes(30)
                .overdueNotificationsEnabled(true)
                .build();
        return notificationPreferencesRepository.save(prefs);
    }

    private UserDTO mapToDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .createdAt(user.getCreatedAt())
                .lastLogin(user.getLastLogin())
                .build();
    }

    private NotificationPreferencesDTO mapToDTO(NotificationPreferences prefs) {
        return NotificationPreferencesDTO.builder()
                .emailEnabled(prefs.isEmailEnabled())
                .popupEnabled(prefs.isPopupEnabled())
                .dailyDigestEnabled(prefs.isDailyDigestEnabled())
                .dailyDigestTime(prefs.getDailyDigestTime())
                .reminderLeadTimeMinutes(prefs.getReminderLeadTimeMinutes())
                .overdueNotificationsEnabled(prefs.isOverdueNotificationsEnabled())
                .build();
    }
}

package com.taskmanager.service;

import com.taskmanager.dto.context.ContextDTO;
import com.taskmanager.dto.context.CreateContextRequest;
import com.taskmanager.entity.Context;
import com.taskmanager.entity.User;
import com.taskmanager.exception.ResourceNotFoundException;
import com.taskmanager.exception.ValidationException;
import com.taskmanager.repository.ContextRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContextService {

    private final ContextRepository contextRepository;
    private final UserService userService;

    public List<ContextDTO> getAllContexts() {
        User user = userService.getCurrentUser();
        return contextRepository.findByUserIdOrDefault(user.getId())
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<ContextDTO> getDefaultContexts() {
        return contextRepository.findByIsDefaultTrue()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public ContextDTO getContextById(Long id) {
        Context context = contextRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Context", "id", id));
        return mapToDTO(context);
    }

    @Transactional
    public ContextDTO createContext(CreateContextRequest request) {
        User user = userService.getCurrentUser();

        // Check if context name already exists for this user
        if (contextRepository.existsByNameAndUserId(request.getName(), user.getId())) {
            throw new ValidationException("Context with this name already exists");
        }

        // Check if trying to create with a default context name
        if (contextRepository.findByName(request.getName())
                .filter(Context::isDefault)
                .isPresent()) {
            throw new ValidationException("Cannot create context with a default context name");
        }

        Context context = Context.builder()
                .name(request.getName())
                .description(request.getDescription())
                .user(user)
                .isDefault(false)
                .build();

        context = contextRepository.save(context);
        log.info("Context created: {} by user: {}", context.getName(), user.getEmail());

        return mapToDTO(context);
    }

    @Transactional
    public void deleteContext(Long id) {
        User user = userService.getCurrentUser();
        Context context = contextRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Context", "id", id));

        if (context.isDefault()) {
            throw new ValidationException("Cannot delete default context");
        }

        contextRepository.delete(context);
        log.info("Context deleted: {} by user: {}", context.getName(), user.getEmail());
    }

    private ContextDTO mapToDTO(Context context) {
        return ContextDTO.builder()
                .id(context.getId())
                .name(context.getName())
                .description(context.getDescription())
                .isDefault(context.isDefault())
                .taskCount(context.getTasks() != null ? context.getTasks().size() : 0)
                .build();
    }
}

package com.taskmanager.config;

import com.taskmanager.entity.Context;
import com.taskmanager.repository.ContextRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final ContextRepository contextRepository;

    @Override
    public void run(String... args) {
        initializeDefaultContexts();
    }

    private void initializeDefaultContexts() {
        if (contextRepository.findByIsDefaultTrue().isEmpty()) {
            log.info("Initializing default context tags...");

            List<Context> defaultContexts = Arrays.asList(
                    Context.builder()
                            .name("@home")
                            .description("Tasks that can be done at home")
                            .isDefault(true)
                            .build(),
                    Context.builder()
                            .name("@work")
                            .description("Tasks that need to be done at work/office")
                            .isDefault(true)
                            .build(),
                    Context.builder()
                            .name("@phone")
                            .description("Tasks that require phone calls")
                            .isDefault(true)
                            .build(),
                    Context.builder()
                            .name("@errands")
                            .description("Tasks that require going out")
                            .isDefault(true)
                            .build(),
                    Context.builder()
                            .name("@computer")
                            .description("Tasks that require a computer")
                            .isDefault(true)
                            .build(),
                    Context.builder()
                            .name("@waiting")
                            .description("Tasks waiting for someone else")
                            .isDefault(true)
                            .build(),
                    Context.builder()
                            .name("@anywhere")
                            .description("Tasks that can be done anywhere")
                            .isDefault(true)
                            .build()
            );

            contextRepository.saveAll(defaultContexts);
            log.info("Default context tags initialized: {} contexts created", defaultContexts.size());
        } else {
            log.info("Default context tags already exist, skipping initialization");
        }
    }
}

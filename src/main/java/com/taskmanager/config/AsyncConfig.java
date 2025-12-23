package com.taskmanager.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration
@EnableAsync
public class AsyncConfig {
    // Spring's default async executor will be used
    // Can be customized if needed for thread pool configuration
}

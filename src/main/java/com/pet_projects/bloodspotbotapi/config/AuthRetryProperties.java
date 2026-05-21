package com.pet_projects.bloodspotbotapi.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "auth.retry")
public class AuthRetryProperties {

    private int maxAttempts = 3;

    private int delayMs = 1000;
}

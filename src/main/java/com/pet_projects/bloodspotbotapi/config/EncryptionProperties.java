package com.pet_projects.bloodspotbotapi.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "encryption")
public class EncryptionProperties {
    private String secretKey;
}

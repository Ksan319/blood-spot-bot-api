package com.pet_projects.bloodspotbotapi.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "errors")
public class ErrorLogProperties {

    private int lastCount = 15;
}

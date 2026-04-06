package com.pet_projects.bloodspotbotapi;

import com.pet_projects.bloodspotbotapi.config.EncryptionProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties(EncryptionProperties.class)
public class BloodSpotBotApiApplication {

        public static void main(String[] args) {
                SpringApplication.run(BloodSpotBotApiApplication.class, args);
        }


}

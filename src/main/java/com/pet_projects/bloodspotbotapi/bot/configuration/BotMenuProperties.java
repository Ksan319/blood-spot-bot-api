package com.pet_projects.bloodspotbotapi.bot.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "commands")
public class BotMenuProperties {

    private HashMap<String, Menu> menus = new HashMap<>();

    @Data
    public static class Menu{
        private String text;
        private List<Button> buttons;
    }

    @Data
    public static class Button{
        private String label;
        private String callBack;
    }
}

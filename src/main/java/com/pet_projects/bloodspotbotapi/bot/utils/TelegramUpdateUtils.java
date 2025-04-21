package com.pet_projects.bloodspotbotapi.bot.utils;

import org.telegram.telegrambots.meta.api.objects.Update;


public class TelegramUpdateUtils {
    public static Long getChatId(Update update) {
        if (update.hasMessage()) {
            return update.getMessage().getChatId();
        } else if (update.hasCallbackQuery()) {
            return update.getCallbackQuery().getMessage().getChatId();
        }
        return null;
    }


    public static String getMessage(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            return update.getMessage().getText();
        } else if (update.hasCallbackQuery()) {
            return update.getCallbackQuery().getData();
        }
        return ""; // или можно выбросить исключение, если это критично
    }
}

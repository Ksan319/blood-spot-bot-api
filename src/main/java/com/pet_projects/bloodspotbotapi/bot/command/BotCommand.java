package com.pet_projects.bloodspotbotapi.bot.command;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;


public interface BotCommand {
    String command(); // например: "/start"
    boolean supports(String message);
    void process(Long chatId, Update update) throws TelegramApiException;

    default InlineKeyboardMarkup getInlineKeyboardMarkup() {
        return null;
    }
}

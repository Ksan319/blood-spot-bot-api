package com.pet_projects.bloodspotbotapi.client;

import com.pet_projects.bloodspotbotapi.service.UserService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Component
@RequiredArgsConstructor
@Slf4j
public class TelegramClientWrapper {
    @Value("${telegram.bot.token}")
    private String token;

    private TelegramClient telegramClient;
    private final UserService userService;

    @PostConstruct
    private void init() {
        telegramClient = new OkHttpTelegramClient(getBotToken());
    }

    private String getBotToken() {
        return token;
    }

    public void sendMessage(SendMessage sendMessage) throws TelegramApiException {
        try {
            telegramClient.execute(sendMessage);
        } catch (TelegramApiException ex) {
            handleBlockedUserIfNeeded(ex, sendMessage.getChatId());
            throw ex;
        }
    }

    public void sendMethod(BotApiMethod<?> method) throws TelegramApiException {
        try {
            telegramClient.execute(method);
        } catch (TelegramApiException ex) {
            String chatId = extractChatId(method);
            handleBlockedUserIfNeeded(ex, chatId);
            throw ex;
        }
    }

    private String extractChatId(BotApiMethod<?> method) {
        if (method instanceof SendMessage m) {
            return m.getChatId();
        }
        if (method instanceof EditMessageText m) {
            return m.getChatId();
        }
        return null;
    }

    private void handleBlockedUserIfNeeded(TelegramApiException ex, String chatIdStr) {
        String msg = ex.getMessage() != null ? ex.getMessage() : "";
        if (msg.toLowerCase().contains("bot was blocked by the user")) {
            try {
                Long chatId = chatIdStr != null ? Long.parseLong(chatIdStr) : null;
                if (chatId != null) {
                    log.warn("User {} blocked the bot. Deleting user.", chatId);
                    userService.deleteUser(chatId);
                }
            } catch (Exception deleteEx) {
                log.error("Failed to delete user after block: {}", deleteEx.getMessage(), deleteEx);
            }
        }
    }
}

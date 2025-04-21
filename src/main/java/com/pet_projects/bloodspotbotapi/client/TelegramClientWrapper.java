package com.pet_projects.bloodspotbotapi.client;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Component
public class TelegramClientWrapper {
    @Value("${telegram.bot.token}")
    private String token;

    private TelegramClient telegramClient;

    @PostConstruct
    private void init() {
        telegramClient = new OkHttpTelegramClient(getBotToken());
    }

    private String getBotToken() {
        return token;
    }

    public void sendMessage(SendMessage sendMessage) throws TelegramApiException {
        telegramClient.execute(sendMessage);
    }

    public void sendMethod(BotApiMethod<?> method) throws TelegramApiException {
        telegramClient.execute(method);
    }
}

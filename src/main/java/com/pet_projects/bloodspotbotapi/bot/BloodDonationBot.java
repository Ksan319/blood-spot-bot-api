package com.pet_projects.bloodspotbotapi.bot;

import com.pet_projects.bloodspotbotapi.bot.handler.UpdateDispatcher;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@RequiredArgsConstructor
public class BloodDonationBot implements SpringLongPollingBot, LongPollingSingleThreadUpdateConsumer {


    @Value("${telegram.bot.token}")
    private String token;


    private final UpdateDispatcher updateDispatcher;


    @Override
    public String getBotToken() {
        return token;
    }

    @Override
    public LongPollingUpdateConsumer getUpdatesConsumer() {
        return this;
    }


    @Override
    public void consume(Update update) {
        updateDispatcher.dispatch(update);
    }
}

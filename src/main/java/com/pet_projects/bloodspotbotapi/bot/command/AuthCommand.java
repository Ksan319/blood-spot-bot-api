package com.pet_projects.bloodspotbotapi.bot.command;

import com.pet_projects.bloodspotbotapi.bot.handler.MenuDispatcher;
import com.pet_projects.bloodspotbotapi.bot.keyboard.CustomKeyBoardBuilder;
import com.pet_projects.bloodspotbotapi.client.TelegramClientWrapper;
import com.pet_projects.bloodspotbotapi.service.session.UserState;
import com.pet_projects.bloodspotbotapi.service.session.UserStateStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
@RequiredArgsConstructor
public class AuthCommand implements BotCommand {
    private final MenuDispatcher menuDispatcher;
    private final UserStateStorage userStateStorage;

    @Override
    public String command() {
        return "/auth";
    }

    @Override
    public boolean supports(String message) {
        return message.equals(command());
    }

    @Override
    public void process(Long chatId, Update update) throws TelegramApiException {
        menuDispatcher.sendMenu("auth", chatId, update);
        userStateStorage.setState(chatId, UserState.AWAITING_AUTH_CREDENTIALS);
    }

}

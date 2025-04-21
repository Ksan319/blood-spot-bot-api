package com.pet_projects.bloodspotbotapi.bot.command;

import com.pet_projects.bloodspotbotapi.bot.handler.MenuDispatcher;
import com.pet_projects.bloodspotbotapi.service.session.UserStateStorage;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@RequiredArgsConstructor
public class StartCommand implements BotCommand {

    private final MenuDispatcher menuDispatcher;
    private final UserStateStorage userStateStorage;

    @Override
    public String command() {
        return "/start";
    }

    @Override
    public boolean supports(String message) {
        return message.equals(command());
    }

    @Override
    @SneakyThrows
    public void process(Long chatId, Update update) {
        menuDispatcher.sendMenu("start", chatId, update);
        userStateStorage.clearState(chatId);
    }


}

package com.pet_projects.bloodspotbotapi.bot.command;

import com.pet_projects.bloodspotbotapi.bot.handler.MenuDispatcher;
import com.pet_projects.bloodspotbotapi.service.UserService;
import com.pet_projects.bloodspotbotapi.service.session.UserStateStorage;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@RequiredArgsConstructor
public class LogoutCommand implements BotCommand {

    private final UserService userService;
    private final UserStateStorage userStateStorage;
    private final StartCommand startCommand;

    @Override
    public String command() {
        return "/logout";
    }

    @Override
    public boolean supports(String message) {
        return message.equals(command());
    }

    @Override
    @SneakyThrows
    public void process(Long chatId, Update update) {
        // удаляем пользователя и его данные
        userService.deleteUser(chatId);
        // очищаем состояние и переводим на стартовый экран
        userStateStorage.clearState(chatId);
        startCommand.process(chatId, update);
    }
}


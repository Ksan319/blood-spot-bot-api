package com.pet_projects.bloodspotbotapi.bot.command;

import com.pet_projects.bloodspotbotapi.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@RequiredArgsConstructor
public class SubscribeCommand implements BotCommand {
    private final UserService userService;
    private final MainCommand mainCommand;

    @Override
    public String command() {
        return "/subscribe";
    }

    @Override
    public boolean supports(String message) {
        return message.equals(command());
    }

    @Override
    @SneakyThrows
    public void process(Long chatId, Update update) {
        try {
            userService.changeSubscription(chatId);
            mainCommand.process(chatId, update);
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }

}

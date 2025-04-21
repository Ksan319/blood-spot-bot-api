package com.pet_projects.bloodspotbotapi.bot.command;

import com.pet_projects.bloodspotbotapi.bot.handler.MenuDispatcher;
import com.pet_projects.bloodspotbotapi.bot.service.MenuService;
import com.pet_projects.bloodspotbotapi.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

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
    public void process(Long chatId, Update update) throws TelegramApiException {
        try {
            userService.changeSubscription(chatId);
            mainCommand.process(chatId, update);
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }

}

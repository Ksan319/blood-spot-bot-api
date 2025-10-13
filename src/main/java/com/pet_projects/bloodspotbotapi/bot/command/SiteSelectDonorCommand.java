package com.pet_projects.bloodspotbotapi.bot.command;

import com.pet_projects.bloodspotbotapi.bot.handler.MenuDispatcher;
import com.pet_projects.bloodspotbotapi.bot.command.MainCommand;
import com.pet_projects.bloodspotbotapi.model.UserSite;
import com.pet_projects.bloodspotbotapi.service.UserService;
import com.pet_projects.bloodspotbotapi.service.session.UserState;
import com.pet_projects.bloodspotbotapi.service.session.UserStateStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
@RequiredArgsConstructor
public class SiteSelectDonorCommand implements BotCommand {
    private final UserService userService;
    private final MenuDispatcher menuDispatcher;
    private final MainCommand mainCommand;
    private final UserStateStorage userStateStorage;

    @Override
    public String command() {
        return "/site_donor";
    }

    @Override
    public boolean supports(String message) {
        return message.equals(command());
    }

    @Override
    public void process(Long chatId, Update update) throws TelegramApiException {
        userService.setUserSite(chatId, UserSite.DONOR_MOS);
        if (userService.hasCredentials(chatId)) {
            userStateStorage.clearState(chatId);
            mainCommand.process(chatId, update);
        } else {
            userStateStorage.setState(chatId, UserState.AWAITING_AUTH_CREDENTIALS);
            menuDispatcher.sendMenu("auth", chatId, update, UserSite.DONOR_MOS.getBaseUrl());
        }
    }
}

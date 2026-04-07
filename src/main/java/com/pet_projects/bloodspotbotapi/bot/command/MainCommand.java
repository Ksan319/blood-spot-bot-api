package com.pet_projects.bloodspotbotapi.bot.command;

import com.pet_projects.bloodspotbotapi.bot.handler.MenuDispatcher;
import com.pet_projects.bloodspotbotapi.model.User;
import com.pet_projects.bloodspotbotapi.repository.UserRepository;
import com.pet_projects.bloodspotbotapi.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@RequiredArgsConstructor
@Slf4j
public class MainCommand implements BotCommand {
    private final UserRepository userRepository;
    private final MenuDispatcher menuDispatcher;
    private final UserService userService;

    @Override
    public String command() {
        return "/main";
    }

    @Override
    public boolean supports(String message) {
        return message.equals(command());
    }

    @Override
    public void process(Long chatId, Update update) {
        User user = userRepository.findById(chatId).orElse(null);
        // TODO: Добавить проверку валидности credentials через AuthService.getCookieHeader(user)
        //       с обработкой AuthFailedException. Сейчас проверяется только наличие email/password.
        if (user == null || !userService.hasCredentials(chatId)) {
            menuDispatcher.sendMenu("authError", chatId, new Update());
            return;
        }
        String subscribeState = user.isSubscribed() ? "✅ Подписка активна" : "❌ Подписка не активна";
        String siteLabel = user.getSite().getDisplayName() + " — " + user.getSite().getValidLocation();
        menuDispatcher.sendMenu("main", chatId, update, user.getEmail(), subscribeState, siteLabel);
    }

}

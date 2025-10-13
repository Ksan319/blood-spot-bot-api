package com.pet_projects.bloodspotbotapi.bot.command;

import com.pet_projects.bloodspotbotapi.bot.handler.MenuDispatcher;
import com.pet_projects.bloodspotbotapi.bot.keyboard.CustomKeyBoardBuilder;
import com.pet_projects.bloodspotbotapi.client.TelegramClientWrapper;
import com.pet_projects.bloodspotbotapi.model.User;
import com.pet_projects.bloodspotbotapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
@RequiredArgsConstructor
@Slf4j
public class MainCommand implements BotCommand{
    private final UserRepository userRepository;
    private final MenuDispatcher menuDispatcher;

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
        User user = userRepository.findById(chatId).get();
        String subscribeState = user.isSubscribed() ? "✅ Подписка активна" : "❌ Подписка не активна";
        String siteLabel;
        if (user.getSite() != null) {
            siteLabel = user.getSite().getDisplayName() + " — " + user.getSite().getValidLocation();
        } else {
            siteLabel = "Поликарпова — https://donor-mos.online/account/";
        }
        menuDispatcher.sendMenu("main", chatId, update, user.getEmail(), subscribeState, siteLabel);
    }

}

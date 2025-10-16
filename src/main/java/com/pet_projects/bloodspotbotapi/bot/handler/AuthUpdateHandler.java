package com.pet_projects.bloodspotbotapi.bot.handler;

import com.pet_projects.bloodspotbotapi.bot.command.MainCommand;
import com.pet_projects.bloodspotbotapi.bot.utils.TelegramUpdateUtils;
import com.pet_projects.bloodspotbotapi.client.TelegramClientWrapper;
import com.pet_projects.bloodspotbotapi.service.AuthService;
import com.pet_projects.bloodspotbotapi.service.UserService;
import com.pet_projects.bloodspotbotapi.service.session.UserState;
import com.pet_projects.bloodspotbotapi.service.session.UserStateStorage;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuthUpdateHandler implements UpdateHandler {

    private final AuthService authService;
    private final UserStateStorage stateStorage;
    private final UserService userService;
    private final MenuDispatcher menuDispatcher;
    private final TelegramClientWrapper telegramClientWrapper;
    private final MainCommand mainCommand;

    @Override
    @SneakyThrows
    public void process(Update update) {
        String message = TelegramUpdateUtils.getMessage(update);
        Long chatId = TelegramUpdateUtils.getChatId(update);

        if (message == null || message.isEmpty()) {
            menuDispatcher.sendMenu("authError", chatId, update);
        }


        String[] parts = message.trim().split("\\s+", 2);
        if (parts.length != 2) {
            menuDispatcher.sendMenu("authError", chatId, update);
            return;
        }

        String username = parts[0];
        String password = parts[1];

        try {
            if (authService.isCredentialValid(chatId, username, password)) {
                userService.saveOrUpdate(chatId, username, password);
                stateStorage.clearState(chatId);
                mainCommand.process(chatId, update);
            } else {
                menuDispatcher.sendMenu("authError", chatId, update);
            }
        } catch (Exception ex) {
            log.error("Ошибка при попытке авторизации пользователя {}: {}", chatId, ex.getMessage(), ex);
            sendText(chatId, "⚠️ Произошла внутренняя ошибка. Попробуйте позже.", null);
        }
    }

    // Public helper to notify user about auth error from other flows (e.g., jobs)
    public void notifyAuthError(Long chatId) {
        try {
            menuDispatcher.sendMenu("authError", chatId, new Update());
        } catch (Exception e) {
            log.error("Failed to notify auth error to user {}: {}", chatId, e.getMessage(), e);
        }
    }

    @Override
    public boolean supports(Update update) {
        Long chatId = TelegramUpdateUtils.getChatId(update);
        if (TelegramUpdateUtils.getMessage(update).startsWith("/start")) {
            return false;
        }
        return stateStorage.getState(chatId) == UserState.AWAITING_AUTH_CREDENTIALS;
    }


    private void sendText(Long chatId, String text, InlineKeyboardMarkup keyboard) throws TelegramApiException {
        SendMessage msg = SendMessage.builder()
                .chatId(chatId)
                .text(text)
                .replyMarkup(keyboard)
                .parseMode("Markdown")
                .build();
        telegramClientWrapper.sendMethod(msg);
    }
}

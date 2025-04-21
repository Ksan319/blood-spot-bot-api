package com.pet_projects.bloodspotbotapi.bot.handler;

import com.pet_projects.bloodspotbotapi.bot.service.MenuService;
import com.pet_projects.bloodspotbotapi.client.TelegramClientWrapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

@Component
@RequiredArgsConstructor
public class MenuDispatcher {
    private final MenuService menuService;
    private final TelegramClientWrapper telegramClientWrapper;

    @SneakyThrows
    public void sendMenu(String key, Long chatId, Update update, Object... args) {
        String text = menuService.getText(key, args);
        InlineKeyboardMarkup keyboardMarkup = menuService.getButtons(key, args);
        if (update.hasMessage()) {
            SendMessage sendMessage = SendMessage.builder()
                    .text(text)
                    .replyMarkup(keyboardMarkup)
                    .parseMode("MARKDOWN")
                    .chatId(chatId)
                    .build();
            telegramClientWrapper.sendMethod(sendMessage);
        } else if (update.hasCallbackQuery()) {
            Integer messageId = update.getCallbackQuery().getMessage().getMessageId();
            EditMessageText editMessageText = EditMessageText.builder()
                    .text(text)
                    .replyMarkup(keyboardMarkup)
                    .chatId(chatId)
                    .messageId(messageId)
                    .parseMode("MARKDOWN")
                    .build();
            telegramClientWrapper.sendMethod(editMessageText);
        } else {
            SendMessage sendMessage = SendMessage.builder()
                    .text(text)
                    .replyMarkup(keyboardMarkup)
                    .parseMode("MARKDOWN")
                    .chatId(chatId)
                    .build();
            telegramClientWrapper.sendMethod(sendMessage);
        }
    }
}


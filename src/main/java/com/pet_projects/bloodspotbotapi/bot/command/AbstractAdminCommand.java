package com.pet_projects.bloodspotbotapi.bot.command;

import com.pet_projects.bloodspotbotapi.bot.client.TelegramClientWrapper;
import com.pet_projects.bloodspotbotapi.service.AdminService;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

@Slf4j
public abstract class AbstractAdminCommand implements BotCommand {
    protected final AdminService adminService;
    protected final TelegramClientWrapper telegramClientWrapper;

    protected AbstractAdminCommand(AdminService adminService, TelegramClientWrapper telegramClientWrapper) {
        this.adminService = adminService;
        this.telegramClientWrapper = telegramClientWrapper;
    }

    protected void sendText(Long chatId, String text) {
        try {
            SendMessage msg = SendMessage.builder()
                    .chatId(chatId)
                    .text(text)
                    .build();
            telegramClientWrapper.sendMethod(msg);
        } catch (Exception e) {
            log.error("Ошибка отправки сообщения админу {}: {}", chatId, e.getMessage(), e);
        }
    }
}

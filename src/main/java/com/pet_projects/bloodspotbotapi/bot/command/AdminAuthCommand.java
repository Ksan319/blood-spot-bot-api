package com.pet_projects.bloodspotbotapi.bot.command;

import com.pet_projects.bloodspotbotapi.bot.client.TelegramClientWrapper;
import com.pet_projects.bloodspotbotapi.service.AdminService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@Slf4j
public class AdminAuthCommand extends AbstractAdminCommand {

    public AdminAuthCommand(AdminService adminService, TelegramClientWrapper telegramClientWrapper) {
        super(adminService, telegramClientWrapper);
    }

    @Override
    public String command() {
        return "/admin-auth";
    }

    @Override
    public boolean supports(String message) {
        return message.equals(command());
    }

    @Override
    public void process(Long chatId, Update update) {
        String message = update.getMessage().getText();
        String[] parts = message.trim().split("\\s+", 2);

        if (parts.length < 2 || parts[1].isBlank()) {
            sendText(chatId, "Неизвестная команда");
            return;
        }

        String password = parts[1].trim();

        if (adminService.verifyPassword(password)) {
            adminService.registerAdmin(chatId);
            sendText(chatId, "Вы авторизованы как админ");
        } else {
            log.warn("Неудачная попытка авторизации админа: chatId={}", chatId);
            sendText(chatId, "Неизвестная команда");
        }
    }
}

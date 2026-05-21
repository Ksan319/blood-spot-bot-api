package com.pet_projects.bloodspotbotapi.bot.command;

import com.pet_projects.bloodspotbotapi.bot.client.TelegramClientWrapper;
import com.pet_projects.bloodspotbotapi.service.AdminService;
import com.pet_projects.bloodspotbotapi.service.SiteErrorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@Slf4j
public class AdminErrorsCommand extends AbstractAdminCommand {

    private final SiteErrorService siteErrorService;

    public AdminErrorsCommand(AdminService adminService, TelegramClientWrapper telegramClientWrapper,
                              SiteErrorService siteErrorService) {
        super(adminService, telegramClientWrapper);
        this.siteErrorService = siteErrorService;
    }

    @Override
    public String command() {
        return "/admin-errors";
    }

    @Override
    public boolean supports(String message) {
        return message.equals(command());
    }

    @Override
    public void process(Long chatId, Update update) {
        if (!adminService.isAdmin(chatId)) {
            sendText(chatId, "Неизвестная команда");
            return;
        }

        String errorsReport = siteErrorService.formatLastErrors();
        sendText(chatId, errorsReport);
    }
}

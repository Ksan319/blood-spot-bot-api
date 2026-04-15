package com.pet_projects.bloodspotbotapi.bot.command;

import com.pet_projects.bloodspotbotapi.bot.client.TelegramClientWrapper;
import com.pet_projects.bloodspotbotapi.repository.UserRepository;
import com.pet_projects.bloodspotbotapi.service.AdminService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@Slf4j
public class AdminStatsCommand extends AbstractAdminCommand {
    private final UserRepository userRepository;

    public AdminStatsCommand(AdminService adminService, TelegramClientWrapper telegramClientWrapper,
                             UserRepository userRepository) {
        super(adminService, telegramClientWrapper);
        this.userRepository = userRepository;
    }

    @Override
    public String command() {
        return "/admin-stats";
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

        long totalUsers = userRepository.count();
        long activeSubscriptions = userRepository.countBySubscribed(true);

        String text = String.format(
                "Статистика:\nВсего пользователей: %d\nАктивных подписок: %d",
                totalUsers, activeSubscriptions
        );
        sendText(chatId, text);
    }
}

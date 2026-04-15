package com.pet_projects.bloodspotbotapi.bot.command;

import com.pet_projects.bloodspotbotapi.bot.client.TelegramClientWrapper;
import com.pet_projects.bloodspotbotapi.repository.UserRepository;
import com.pet_projects.bloodspotbotapi.service.AdminService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

@Component
@Slf4j
public class AdminSayCommand extends AbstractAdminCommand {
    private static final long BROADCAST_DELAY_MS = 50;

    private final UserRepository userRepository;

    public AdminSayCommand(AdminService adminService, TelegramClientWrapper telegramClientWrapper,
                           UserRepository userRepository) {
        super(adminService, telegramClientWrapper);
        this.userRepository = userRepository;
    }

    @Override
    public String command() {
        return "/admin-say";
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

        String message = update.getMessage().getText();
        String[] parts = message.trim().split("\\s+", 2);

        if (parts.length < 2 || parts[1].isBlank()) {
            sendText(chatId, "Неизвестная команда");
            return;
        }

        String text = parts[1].trim();
        List<Long> chatIds = userRepository.findAllIds();
        int total = chatIds.size();
        int sent = 0;

        for (Long userChatId : chatIds) {
            try {
                SendMessage msg = SendMessage.builder()
                        .chatId(userChatId)
                        .text(text)
                        .build();
                telegramClientWrapper.sendMethod(msg);
                sent++;
                Thread.sleep(BROADCAST_DELAY_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("Рассылка прервана: отправлено {} из {}", sent, total);
                break;
            } catch (Exception e) {
                log.warn("Не удалось отправить сообщение пользователю {}: {}", userChatId, e.getMessage());
            }
        }

        sendText(chatId, String.format("Сообщение отправлено %d из %d пользователям", sent, total));
    }
}

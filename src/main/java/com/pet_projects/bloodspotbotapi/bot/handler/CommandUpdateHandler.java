package com.pet_projects.bloodspotbotapi.bot.handler;

import com.pet_projects.bloodspotbotapi.bot.command.BotCommand;
import com.pet_projects.bloodspotbotapi.bot.utils.TelegramUpdateUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

/**
 * Обработчик входящих Telegram-апдейтов с командами.
 * Определяет, является ли сообщение командой (начинается с "/"),
 * и передаёт управление соответствующему BotCommand.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CommandUpdateHandler implements UpdateHandler {
    /**
     * Список всех зарегистрированных команд бота.
     */
    private final List<BotCommand> commands;

    /**
     * Проверяет, поддерживает ли этот обработчик данный апдейт:
     * считается командой, если текст сообщения начинается с "/".
     *
     * @param update входящее обновление Telegram
     * @return true, если это команда
     */
    @Override
    public boolean supports(Update update) {
        String message = TelegramUpdateUtils.getMessage(update);
        return message != null && message.startsWith("/");
    }

    /**
     * Обрабатывает команду: логирует принятую строку,
     * находит соответствующий BotCommand и вызывает его логику.
     * Если команда не найдена, логирует предупреждение.
     *
     * @param update входящее обновление Telegram
     */
    @Override
    public void process(Update update) {
        String message = TelegramUpdateUtils.getMessage(update);
        Long chatId = TelegramUpdateUtils.getChatId(update);

        log.info("Получена команда: {} из чата {}", message, chatId);
        // Извлекаем имя команды до первого пробела
        String commandName = message.split("\\s+", 2)[0];

        // Находим и выполняем первую подходящую команду
        boolean handled = commands.stream()
                .filter(cmd -> cmd.supports(commandName))
                .findFirst()
                .map(cmd -> {
                    try {
                        cmd.process(chatId, update);
                    } catch (TelegramApiException e) {
                        throw new RuntimeException(e);
                    }
                    return true;
                })
                .orElse(false);

        if (!handled) {
            log.warn("Не найдена реализация команды: {}", commandName);
        }
    }
}

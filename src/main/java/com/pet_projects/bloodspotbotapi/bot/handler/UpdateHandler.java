package com.pet_projects.bloodspotbotapi.bot.handler;

import org.telegram.telegrambots.meta.api.objects.Update;

public interface UpdateHandler {
    boolean supports(Update update);
    void process(Update update);
}

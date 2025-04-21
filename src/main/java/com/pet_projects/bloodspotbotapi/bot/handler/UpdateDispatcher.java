package com.pet_projects.bloodspotbotapi.bot.handler;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

@Component
@RequiredArgsConstructor
public class UpdateDispatcher {
    private final List<UpdateHandler> updateHandlers;

    public void dispatch(Update update) {
        for (UpdateHandler updateHandler : updateHandlers) {
            if (updateHandler.supports(update)) {
                updateHandler.process(update);
                break;
            }
        }
    }
}

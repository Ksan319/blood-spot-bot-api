package com.pet_projects.bloodspotbotapi.bot.handler;

import com.pet_projects.bloodspotbotapi.client.TelegramClientWrapper;
import com.pet_projects.bloodspotbotapi.model.Spot;
import com.pet_projects.bloodspotbotapi.model.User;
import com.pet_projects.bloodspotbotapi.service.SpotService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class NewSpotHandler {
    private final SpotService spotService;
    private final TelegramClientWrapper telegramClientWrapper;
    private final MenuDispatcher menuDispatcher;

    @SneakyThrows
    public void sendNewSpots(User user) {
        log.info("Sending new spots for {}", user.getEmail());
        List<Spot> actualSpots = spotService.getSpotsForUser(user);
        if (!actualSpots.isEmpty()) {
            StringBuilder spotText = new StringBuilder();
            spotText.append("\n");
            for (Spot spot : actualSpots) {
                spotText.append(" - ").append(spot.getSpotDate()).append("\n");
                spot.setSend(true);
            }

            log.info("spot text {}", spotText.toString());
            menuDispatcher.sendMenu("newSpotsFound", user.getId(), new Update(), spotText.toString());
            spotService.markSpotIsSent(actualSpots);
        }
    }
}

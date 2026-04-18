package com.pet_projects.bloodspotbotapi.bot.handler;

import com.pet_projects.bloodspotbotapi.model.Spot;
import com.pet_projects.bloodspotbotapi.model.User;
import com.pet_projects.bloodspotbotapi.model.UserSite;
import com.pet_projects.bloodspotbotapi.service.SpotService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class NewSpotHandler {
    private final SpotService spotService;
    private final MenuDispatcher menuDispatcher;

    @SneakyThrows
    public void sendNewSpots(User user) {
        log.info("Sending new spots for {} (id={})", user.getEmail(), user.getId());
        List<Spot> actualSpots = spotService.getSpotsForUser(user);
        if (actualSpots.isEmpty()) {
            log.info("No unsent spots for user {}. Nothing to notify.", user.getEmail());
            return;
        }

        log.info("Unsent spots count for {}: {}", user.getEmail(), actualSpots.size());

        if (user.getSite().isAll()) {
            sendMultiSiteSpots(user, actualSpots);
        } else {
            sendSingleSiteSpots(user, actualSpots);
        }
    }

    private void sendSingleSiteSpots(User user, List<Spot> spots) {
        StringBuilder spotText = new StringBuilder();
        spotText.append("\n");
        for (Spot spot : spots) {
            spotText.append(" - ").append(spot.getSpotDate()).append("\n");
            spot.setSend(true);
        }
        String siteUrl = user.getSite().getValidLocation();
        menuDispatcher.sendMenu("newSpotsFound", user.getId(), new Update(), spotText.toString(), siteUrl);
        spotService.markSpotIsSent(spots);
        log.info("Notification sent for {} dates to user {}", spots.size(), user.getEmail());
    }

    private void sendMultiSiteSpots(User user, List<Spot> spots) {
        Map<UserSite, List<Spot>> bySite = spots.stream()
                .collect(Collectors.groupingBy(Spot::getEffectiveSite));

        for (Map.Entry<UserSite, List<Spot>> entry : bySite.entrySet()) {
            UserSite site = entry.getKey();
            List<Spot> siteSpots = entry.getValue();

            StringBuilder spotText = new StringBuilder();
            spotText.append("\n📍 ").append(site.getDisplayName()).append(":\n");
            for (Spot spot : siteSpots) {
                spotText.append(" - ").append(spot.getSpotDate()).append("\n");
                spot.setSend(true);
            }
            String siteUrl = site.getValidLocation();
            menuDispatcher.sendMenu("newSpotsFound", user.getId(), new Update(), spotText.toString(), siteUrl);
            spotService.markSpotIsSent(siteSpots);
            log.info("Notification sent for {} dates at site {} to user {}", siteSpots.size(), site, user.getEmail());
        }
    }
}

package com.pet_projects.bloodspotbotapi.service.jobs;

import com.pet_projects.bloodspotbotapi.bot.handler.NewSpotHandler;
import com.pet_projects.bloodspotbotapi.client.donormos.DonorMosOnlineClient;
import com.pet_projects.bloodspotbotapi.model.Spot;
import com.pet_projects.bloodspotbotapi.model.User;
import com.pet_projects.bloodspotbotapi.repository.SpotRepository;
import com.pet_projects.bloodspotbotapi.repository.UserRepository;
import com.pet_projects.bloodspotbotapi.service.SpotService;
import com.pet_projects.bloodspotbotapi.service.dto.SpotDTO;
import com.pet_projects.bloodspotbotapi.service.session.CookieSessionManager;
import com.pet_projects.bloodspotbotapi.utils.SpotUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.jsoup.Jsoup;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Log4j2
@RequiredArgsConstructor
public class SpotDonationJob {

    private final DonorMosOnlineClient donorMosClient;
    private final CookieSessionManager sessionManager;
    private final UserRepository userRepository;
    private final SpotService spotService;
    private final NewSpotHandler newSpotHandler;
    /**
     * Для каждого юзера делает auth + getSpots
     */
    public String fetchSpotsFor(User user) {
        String cookieHeader = sessionManager.getCookieHeader(user);
        ResponseEntity<String> resp = donorMosClient.getSpotsWithCookie(cookieHeader);
        if (resp.getStatusCode().is2xxSuccessful()) {
            return resp.getBody();
        }
        throw new RuntimeException("Failed to fetch spots, status=" + resp.getStatusCode());
    }


    /**
     * Джоба, запускается каждый час для всех юзеров
     */
    @Scheduled(cron = "0 */15 * * * *")
    public void pollAllUsers() {
        log.info("Starting scheduled polling of subscribed users...");
        List<User> users = userRepository.findBySubscribed(true).orElse(List.of());
        List<User> subscribedUsers = users.stream().filter(User::isSubscribed).toList();
        log.info("Found {} subscribed users to poll", subscribedUsers.size());
        for (User u : subscribedUsers) {
            log.info("Start searching spots for user: {}", u.getEmail());
            try {
                String html = fetchSpotsFor(u);
                List<SpotDTO> findSpots = SpotUtils.getSpots(Jsoup.parse(html).getElementsByClass("dates-table__item table-item"));
                spotService.saveNewSpots(findSpots, u);
                newSpotHandler.sendNewSpots(u);
                log.info("User {} → Found {} new spots", u.getEmail(), findSpots.size());
            } catch (Exception e) {
                log.error("Error while processing user {}: {}", u.getEmail(), e.getMessage(), e);
            }
        }
        log.info("Scheduled polling completed.");
    }
}
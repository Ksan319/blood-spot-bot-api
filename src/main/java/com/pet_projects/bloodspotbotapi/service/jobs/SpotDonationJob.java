package com.pet_projects.bloodspotbotapi.service.jobs;

import com.pet_projects.bloodspotbotapi.bot.handler.AuthUpdateHandler;
import com.pet_projects.bloodspotbotapi.bot.handler.NewSpotHandler;
import com.pet_projects.bloodspotbotapi.client.donormos.DonorMosOnlineClient;
import com.pet_projects.bloodspotbotapi.model.User;
import com.pet_projects.bloodspotbotapi.model.UserSite;
import com.pet_projects.bloodspotbotapi.repository.UserRepository;
import com.pet_projects.bloodspotbotapi.service.SpotService;
import com.pet_projects.bloodspotbotapi.service.UserService;
import com.pet_projects.bloodspotbotapi.service.SiteErrorService;
import com.pet_projects.bloodspotbotapi.service.exception.AuthFailedException;
import com.pet_projects.bloodspotbotapi.service.exception.SiteUnavailableException;
import com.pet_projects.bloodspotbotapi.service.dto.SpotDTO;
import com.pet_projects.bloodspotbotapi.service.AuthService;
import com.pet_projects.bloodspotbotapi.utils.SpotUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.jsoup.Jsoup;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Log4j2
@RequiredArgsConstructor
public class SpotDonationJob {

    private final DonorMosOnlineClient donorMosClient;
    private final AuthService authService;
    private final UserRepository userRepository;
    private final SpotService spotService;
    private final NewSpotHandler newSpotHandler;
    private final UserService userService;
    private final AuthUpdateHandler authUpdateHandler;
    private final SiteErrorService siteErrorService;

    public String fetchSpotsFor(User user, UserSite site) {
        String cookieHeader = authService.getCookieHeader(user, site);
        String baseUrl = site.getBaseUrl();
        log.info("Fetching spots: user={}, id={}, site={}, baseUrl={}", user.getEmail(), user.getId(), site, baseUrl);
        ResponseEntity<String> resp = donorMosClient.getAccountPage(baseUrl, cookieHeader);
        if (resp.getStatusCode().is2xxSuccessful()) {
            String body = resp.getBody();
            int len = body != null ? body.length() : 0;
            log.info("Fetched spots OK: user={}, site={}, status={}, bodyLen={}", user.getEmail(), site, resp.getStatusCode(), len);
            return body;
        }
        log.warn("Failed to fetch spots: user={}, site={}, status={}", user.getEmail(), site, resp.getStatusCode());
        throw new RuntimeException("Failed to fetch spots for site=" + site + ", status=" + resp.getStatusCode());
    }

    @Scheduled(cron = "0 */5 * * * *")
    public void pollAllUsers() {
        log.info("Starting scheduled polling of subscribed users...");
        List<User> users = userRepository.findBySubscribed(true).orElse(List.of());
        List<User> subscribedUsers = users.stream().filter(User::isSubscribed).toList();
        log.info("Found {} subscribed users to poll", subscribedUsers.size());
        for (User u : subscribedUsers) {
            log.info("Polling user start: {} (id={})", u.getEmail(), u.getId());
            List<UserSite> sites = u.getSite().getIndividualSites();
            List<UserSite> authFailedSites = new ArrayList<>();
            List<UserSite> unavailableSites = new ArrayList<>();
            for (UserSite site : sites) {
                try {
                    String html = fetchSpotsFor(u, site);
                    var elements = Jsoup.parse(html).getElementsByClass("dates-table__item table-item");
                    List<SpotDTO> findSpots = SpotUtils.getSpots(elements);
                    log.info("Parsed page: user={}, site={}, nodesFound={}, spotDatesExtracted={}", u.getEmail(), u.getId(),
                            site, elements.size(), findSpots.size());
                    spotService.saveNewSpots(findSpots, u, site);
                } catch (SiteUnavailableException e) {
                    unavailableSites.add(site);
                    siteErrorService.logSiteUnavailable(u, site, e.getMessage());
                    log.warn("Site unavailable for user {} (id={}) on site {}, skipping: {}", u.getEmail(), u.getId(), site, e.getMessage());
                } catch (AuthFailedException e) {
                    authFailedSites.add(site);
                    siteErrorService.logAuthFailed(u, site, e.getMessage());
                    if (u.getSite().isAll()) {
                        log.warn("Auth failed for user {} (id={}) on site {}, skipping: {}", u.getEmail(), u.getId(), site, e.getMessage());
                    } else {
                        log.warn("Auth failed for user {} (id={}), notifying and logging out: {}", u.getEmail(), u.getId(), e.getMessage());
                        authUpdateHandler.notifyAuthError(u.getId());
                        userService.deleteUser(u.getId());
                        break;
                    }
                } catch (Exception e) {
                    unavailableSites.add(site);
                    siteErrorService.logSiteUnavailable(u, site, e.getMessage());
                    log.error("Error while processing user {} on site {}: {}", u.getEmail(), u.getId(), site, e.getMessage(), e);
                }
            }
            if (userRepository.findById(u.getId()).isPresent()) {
                if (u.getSite().isAll() && authFailedSites.size() == sites.size()) {
                    log.warn("Auth failed for user {} (id={}) on ALL sites (site unavailable on {}), notifying and logging out",
                            u.getEmail(), u.getId(), unavailableSites.size());
                    authUpdateHandler.notifyAuthError(u.getId());
                    userService.deleteUser(u.getId());
                } else {
                    spotService.cleanupOrphanedSpots(u, sites);
                    newSpotHandler.sendNewSpots(u);
                    log.info("Polling user done: {} (id={}), unavailableSites={}", u.getEmail(), u.getId(), unavailableSites.size());
                }
            }
        }
        log.info("Scheduled polling completed.");
    }
}

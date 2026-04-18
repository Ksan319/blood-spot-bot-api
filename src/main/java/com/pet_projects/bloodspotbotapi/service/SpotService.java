package com.pet_projects.bloodspotbotapi.service;

import com.pet_projects.bloodspotbotapi.model.Spot;
import com.pet_projects.bloodspotbotapi.model.User;
import com.pet_projects.bloodspotbotapi.model.UserSite;
import com.pet_projects.bloodspotbotapi.repository.SpotRepository;
import com.pet_projects.bloodspotbotapi.service.dto.SpotDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SpotService {
    private final SpotRepository spotRepository;

    public List<Spot> getSpotsForUser(User user) {
        return spotRepository.findAllByIsSendFalseAndUser(user).orElse(List.of());
    }

    public void markSpotIsSent(@NonNull List<Spot> spots) {
        spotRepository.saveAll(spots);
    }

    @SuppressWarnings("null")
    public void saveNewSpots(List<SpotDTO> spots, User user, UserSite site) {
        List<Spot> existingSpots = spotRepository.findAllByUser(user);

        List<Spot> existingForSite = existingSpots.stream()
                .filter(s -> siteMatches(s, site))
                .toList();

        List<LocalDate> existingDates = existingForSite.stream()
                .map(Spot::getSpotDate)
                .toList();

        List<LocalDate> foundDates = spots.stream()
                .map(SpotDTO::getSpotDate)
                .toList();

        List<Spot> toDelete = existingForSite.stream()
                .filter(spot -> !foundDates.contains(spot.getSpotDate()))
                .toList();
        if (!toDelete.isEmpty()) {
            spotRepository.deleteAll(toDelete);
        }

        List<Spot> toAddDates = spots.stream()
                .filter(dto -> !existingDates.contains(dto.getSpotDate()))
                .map(dto -> {
                    Spot newSpot = new Spot();
                    newSpot.setSpotDate(dto.getSpotDate());
                    newSpot.setUser(user);
                    newSpot.setSite(site);
                    return newSpot;
                })
                .toList();
        spotRepository.saveAll(toAddDates);

        log.info("Spot sync for user {} (id={}), site={}: existing={}, parsed={}, added={}, removed={}",
                user.getEmail(), user.getId(), site, existingForSite.size(), foundDates.size(),
                toAddDates.size(), toDelete.size());
    }

    public void cleanupOrphanedSpots(User user, List<UserSite> activeSites) {
        List<Spot> allSpots = spotRepository.findAllByUser(user);
        List<Spot> orphaned = allSpots.stream()
                .filter(s -> s.getSite() != null && !activeSites.contains(s.getSite()))
                .toList();
        if (!orphaned.isEmpty()) {
            spotRepository.deleteAll(orphaned);
            log.info("Cleaned up {} orphaned spots for user {} (id={})", orphaned.size(), user.getEmail(), user.getId());
        }
    }

    @SuppressWarnings("null")
    public void backfillNullSites() {
        List<Spot> nullSiteSpots = spotRepository.findAllBySiteNull();
        if (nullSiteSpots.isEmpty()) {
            return;
        }
        for (Spot spot : nullSiteSpots) {
            if (spot.getUser() != null && spot.getUser().getSite() != null) {
                spot.setSite(spot.getUser().getSite());
            }
        }
        spotRepository.saveAll(nullSiteSpots);
        log.info("Backfilled site field for {} spots", nullSiteSpots.size());
    }

    private boolean siteMatches(Spot spot, UserSite site) {
        if (spot.getSite() != null) {
            return spot.getSite() == site;
        }
        return spot.getUser() != null && spot.getUser().getSite() == site;
    }
}

package com.pet_projects.bloodspotbotapi.service;

import com.pet_projects.bloodspotbotapi.model.Spot;
import com.pet_projects.bloodspotbotapi.model.User;
import com.pet_projects.bloodspotbotapi.repository.SpotRepository;
import com.pet_projects.bloodspotbotapi.service.dto.SpotDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.lang.NonNull;

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
    public void saveNewSpots(List<SpotDTO> spots, User user) {
        // Получаем все текущие споты пользователя
        List<Spot> existingSpots = spotRepository.findAllByUser(user);

        // Карта текущих дат
        List<LocalDate> existingDates = existingSpots.stream()
                .map(Spot::getSpotDate)
                .toList();

        // Даты, обнаруженные при текущем обходе
        List<LocalDate> foundDates = spots.stream()
                .map(SpotDTO::getSpotDate)
                .toList();

        // Удаляем споты, которые пропали на сайте
        List<Spot> toDelete = existingSpots.stream()
                .filter(spot -> !foundDates.contains(spot.getSpotDate()))
                .toList();
        if (!toDelete.isEmpty()) {
            spotRepository.deleteAll(toDelete);
        }

        // Создаём новые споты для появившихся дат
        List<Spot> toAddDates = spots.stream()
                .filter(dto -> !existingDates.contains(dto.getSpotDate()))
                .map(dto -> {
                    Spot newSpot = new Spot();
                    newSpot.setSpotDate(dto.getSpotDate());
                    newSpot.setUser(user);
                    return newSpot;
                })
                .toList();
        spotRepository.saveAll(toAddDates);

        log.info("Spot sync for user {} (id={}): existing={}, parsed={}, added={}, removed={}",
                user.getEmail(), user.getId(), existingSpots.size(), foundDates.size(), toAddDates.size(),
                toDelete.size());
    }
}

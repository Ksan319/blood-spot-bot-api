package com.pet_projects.bloodspotbotapi.service;

import com.pet_projects.bloodspotbotapi.model.Spot;
import com.pet_projects.bloodspotbotapi.model.User;
import com.pet_projects.bloodspotbotapi.repository.SpotRepository;
import com.pet_projects.bloodspotbotapi.service.dto.SpotDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SpotService {
    private final SpotRepository spotRepository;

    public List<Spot> getSpotsForUser(User user) {
        return spotRepository.findAllByIsSendFalseAndUser(user).orElse(List.of());
    }

    public void markSpotIsSent(List<Spot> spots) {
        spotRepository.saveAll(spots);
    }

    public void saveNewSpots(List<SpotDTO> spots, User user) {
        // Получаем текущие даты уже сохранённых пятен
        List<LocalDate> existingDates = spotRepository.findAllByUser(user)
                .stream()
                .map(Spot::getSpotDate)
                .toList();

        // Фильтруем только те, которых ещё нет
        spots.stream()
                .filter(dto -> !existingDates.contains(dto.getSpotDate()))
                .forEach(dto -> {
                    Spot newSpot = new Spot();
                    newSpot.setSpotDate(dto.getSpotDate());
                    newSpot.setUser(user);
                    spotRepository.save(newSpot);
                });
    }
}

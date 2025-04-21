package com.pet_projects.bloodspotbotapi.repository;

import com.pet_projects.bloodspotbotapi.model.Spot;
import com.pet_projects.bloodspotbotapi.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SpotRepository extends JpaRepository<Spot, Long> {
    Optional<List<Spot>> findAllByIsSendFalseAndUser(User user);

    List<Spot> findAllByUser(User user);
}

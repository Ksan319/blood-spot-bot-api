package com.pet_projects.bloodspotbotapi.repository;

import com.pet_projects.bloodspotbotapi.model.Admin;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminRepository extends JpaRepository<Admin, Long> {
    boolean existsById(Long chatId);
}

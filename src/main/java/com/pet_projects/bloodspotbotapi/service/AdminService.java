package com.pet_projects.bloodspotbotapi.service;

import com.pet_projects.bloodspotbotapi.model.Admin;
import com.pet_projects.bloodspotbotapi.repository.AdminRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class AdminService {
    private final AdminRepository adminRepository;

    @Value("${admin.password}")
    private String adminPassword;

    public boolean isAdmin(Long chatId) {
        return adminRepository.existsById(chatId);
    }

    public boolean verifyPassword(String password) {
        return adminPassword.equals(password);
    }

    public void registerAdmin(Long chatId) {
        Admin admin = Admin.builder()
                .id(chatId)
                .createdAt(LocalDateTime.now())
                .build();
        adminRepository.save(admin);
        log.info("Зарегистрирован новый админ: chatId={}", chatId);
    }
}

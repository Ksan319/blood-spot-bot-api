package com.pet_projects.bloodspotbotapi.service.session;

import com.pet_projects.bloodspotbotapi.model.User;
import com.pet_projects.bloodspotbotapi.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CookieSessionManager {

    private final AuthService authService;


    public String getCookieHeader(User user) {
        // Delegate to AuthService to avoid duplication
        return authService.getCookieHeader(user);
    }
}

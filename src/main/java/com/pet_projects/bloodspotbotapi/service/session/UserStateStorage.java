package com.pet_projects.bloodspotbotapi.service.session;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class UserStateStorage {
    private final Map<Long, UserState> userStates = new ConcurrentHashMap<>();

    public void setState(Long chatId, UserState state) {
        userStates.put(chatId, state);
    }

    public UserState getState(Long chatId) {
        return userStates.getOrDefault(chatId, UserState.NONE);
    }

    public void clearState(Long chatId) {
        userStates.remove(chatId);
    }
}

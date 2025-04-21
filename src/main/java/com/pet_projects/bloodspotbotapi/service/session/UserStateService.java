package com.pet_projects.bloodspotbotapi.service.session;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserStateService {
    private final UserStateStorage userStateStorage;


    public void startSession(Long chatId, UserState state) {
        userStateStorage.setState(chatId, state);
    }

    public void endSession(Long chatId) {
        userStateStorage.clearState(chatId);
    }

    public UserState getState(Long chatId) {
        return userStateStorage.getState(chatId);
    }
}

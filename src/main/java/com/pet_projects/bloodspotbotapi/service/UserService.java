package com.pet_projects.bloodspotbotapi.service;

import com.pet_projects.bloodspotbotapi.model.User;
import com.pet_projects.bloodspotbotapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public void saveOrUpdate(Long chatId, String login, String password) {
        User user = userRepository.findById(chatId)
                .orElseGet(() -> {
                    User u = new User();
                    u.setId(chatId);
                    return u;
                });

        user.setEmail(login);
        user.setPassword(password);
        user.setActive(true);
        userRepository.save(user);
    }

    public void changeSubscription(Long chatId) {
        User user = userRepository.findById(chatId).get();
        user.setSubscribed(!user.isSubscribed());
        userRepository.save(user);
    }
}

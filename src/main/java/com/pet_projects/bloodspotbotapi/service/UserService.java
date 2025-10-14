package com.pet_projects.bloodspotbotapi.service;

import com.pet_projects.bloodspotbotapi.model.User;
import com.pet_projects.bloodspotbotapi.model.UserSite;
import com.pet_projects.bloodspotbotapi.repository.UserRepository;
import com.pet_projects.bloodspotbotapi.repository.SpotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final UserRepository userRepository;
    private final SpotRepository spotRepository;

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
        if (user.getSite() == null) {
            user.setSite(UserSite.DONOR_MOS);
        }
        userRepository.save(user);
    }

    public void changeSubscription(Long chatId) {
        User user = userRepository.findById(chatId).get();
        user.setSubscribed(!user.isSubscribed());
        userRepository.save(user);
    }

    public void setUserSite(Long chatId, UserSite site) {
        User user = userRepository.findById(chatId)
                .orElseGet(() -> User.builder()
                        .id(chatId)
                        .active(false)
                        .subscribed(true)
                        .build());
        user.setSite(site);
        userRepository.save(user);
    }

    public boolean hasCredentials(Long chatId) {
        return userRepository.findById(chatId)
                .map(u -> u.getEmail() != null && !u.getEmail().isBlank()
                        && u.getPassword() != null && !u.getPassword().isBlank())
                .orElse(false);
    }

    public void deleteUser(Long chatId) {
        userRepository.findById(chatId).ifPresent(user -> {
            try {
                var spots = spotRepository.findAllByUser(user);
                int spotCount = spots.size();
                spotRepository.deleteAll(spots);
                userRepository.delete(user);
                log.info("Deleted user id={} email={} with {} spots", user.getId(), user.getEmail(), spotCount);
            } catch (Exception ex) {
                log.error("Failed to delete user id={}: {}", chatId, ex.getMessage(), ex);
                throw ex;
            }
        });
    }
}

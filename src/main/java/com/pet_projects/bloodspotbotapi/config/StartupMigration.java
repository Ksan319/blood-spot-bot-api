package com.pet_projects.bloodspotbotapi.config;

import com.pet_projects.bloodspotbotapi.model.User;
import com.pet_projects.bloodspotbotapi.model.UserSite;
import com.pet_projects.bloodspotbotapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class StartupMigration implements ApplicationRunner {
    private final UserRepository userRepository;

    @Override
    public void run(ApplicationArguments args) {
        List<User> users = userRepository.findAll();
        long updated = users.stream()
                .filter(u -> u.getSite() == null)
                .peek(u -> u.setSite(UserSite.DONOR_MOS))
                .count();
        if (updated > 0) {
            userRepository.saveAll(users);
            log.info("Backfilled default site for {} users", updated);
        }
    }
}


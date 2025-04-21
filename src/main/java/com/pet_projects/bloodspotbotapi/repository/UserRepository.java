package com.pet_projects.bloodspotbotapi.repository;

import com.pet_projects.bloodspotbotapi.model.User;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    @NotNull Optional<User> findById(@NotNull Long id);

    Optional<List<User>> findBySubscribed(boolean subscribed);

}

package com.pet_projects.bloodspotbotapi.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Data
@Builder
@AllArgsConstructor
public class User {
    @Id
    private Long id;

    private String email;

    private String password;

    private boolean active;

    private boolean subscribed = true;

    public User() {

    }
}

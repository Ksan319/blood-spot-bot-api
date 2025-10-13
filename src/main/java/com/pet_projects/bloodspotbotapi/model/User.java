package com.pet_projects.bloodspotbotapi.model;

import jakarta.persistence.*;
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

    @Enumerated(EnumType.STRING)
    @Column(name = "site")
    private UserSite site;

    public User() {

    }
}

package com.pet_projects.bloodspotbotapi.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "site_errors")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SiteError {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "user_email")
    private String userEmail;

    @Enumerated(EnumType.STRING)
    @Column(name = "site")
    private UserSite site;

    @Column(name = "error_type")
    private String errorType;

    @Column(name = "error_message", length = 1024)
    private String errorMessage;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}

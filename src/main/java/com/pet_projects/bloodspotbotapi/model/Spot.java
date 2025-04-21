package com.pet_projects.bloodspotbotapi.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;

@Data
@Entity
@NoArgsConstructor
@RequiredArgsConstructor
public class Spot {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String uuid;
    @NotNull
    private LocalDate spotDate;

    private boolean isSend = false;

    @NotNull
    @ManyToOne(fetch = FetchType.EAGER)
    private User user;

}

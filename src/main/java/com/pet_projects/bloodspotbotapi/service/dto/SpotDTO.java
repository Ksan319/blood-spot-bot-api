package com.pet_projects.bloodspotbotapi.service.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class SpotDTO {
    public LocalDate spotDate;
}

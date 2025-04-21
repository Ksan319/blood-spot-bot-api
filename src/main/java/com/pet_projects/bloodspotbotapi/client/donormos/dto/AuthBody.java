package com.pet_projects.bloodspotbotapi.client.donormos.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import org.springframework.stereotype.Component;

@Data
@Builder
public class AuthBody {
    private String log;
    private String pwd;
    @Builder.Default
    private String wpSubmit = "";
    @Builder.Default
    private String redirect_to = "https://donor-mos.online/account/";
}

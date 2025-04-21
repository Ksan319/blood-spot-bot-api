package com.pet_projects.bloodspotbotapi.client.donormos;

import com.pet_projects.bloodspotbotapi.client.donormos.dto.AuthBody;
import com.pet_projects.bloodspotbotapi.utils.FormUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

@Component
public class DonorMosOnlineClient {

    private final RestClient restClient = RestClient.builder()
            .baseUrl("https://donor-mos.online")
            .build();

    public ResponseEntity<String> auth(AuthBody authBody) {
        MultiValueMap<String, String> form = FormUtils.toFormData(authBody);

        return restClient.post()
                .uri("/auth.php")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .toEntity(String.class);
    }

    public ResponseEntity<String> getSpotsWithCookie(String cookieHeader) {
        return restClient.get()
                .uri("/account/")
                .header(HttpHeaders.COOKIE, cookieHeader)
                .retrieve()
                .toEntity(String.class);
    }
}

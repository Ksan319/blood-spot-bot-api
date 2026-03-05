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

    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/125.0.0.0 Safari/537.36";

    private RestClient buildClient(String baseUrl) {
        return RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    public ResponseEntity<String> getLoginPage(String baseUrl, String cookies) {
        RestClient.RequestHeadersSpec<?> spec = buildClient(baseUrl)
                .get()
                .uri("/auth.php")
                .header(HttpHeaders.USER_AGENT, USER_AGENT)
                .header(HttpHeaders.ACCEPT, "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .header(HttpHeaders.ACCEPT_LANGUAGE, "ru-RU,ru;q=0.9,en-US;q=0.8,en;q=0.7");
        if (cookies != null && !cookies.isBlank()) {
            spec.header(HttpHeaders.COOKIE, cookies);
        }
        return spec.retrieve().toEntity(String.class);
    }

    public ResponseEntity<String> getAbsoluteUrl(String absoluteUrl, String cookies) {
        RestClient.RequestHeadersSpec<?> spec = RestClient.builder().build()
                .get()
                .uri(absoluteUrl)
                .header(HttpHeaders.USER_AGENT, USER_AGENT);
        if (cookies != null && !cookies.isBlank()) {
            spec.header(HttpHeaders.COOKIE, cookies);
        }
        return spec.retrieve().toEntity(String.class);
    }

    public ResponseEntity<String> auth(AuthBody authBody, String baseUrl, String cookies) {
        MultiValueMap<String, String> form = FormUtils.toFormData(authBody);

        RestClient.RequestHeadersSpec<?> spec = buildClient(baseUrl)
                .post()
                .uri("/auth.php")
                .header(HttpHeaders.USER_AGENT, USER_AGENT)
                .header(HttpHeaders.REFERER, baseUrl + "/auth.php")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form);

        if (cookies != null && !cookies.isBlank()) {
            spec.header(HttpHeaders.COOKIE, cookies);
        }

        return spec.retrieve().toEntity(String.class);
    }

    public ResponseEntity<String> getAccountPage(String baseUrl, String cookies) {
        return buildClient(baseUrl)
                .get()
                .uri("/account/")
                .header(HttpHeaders.USER_AGENT, USER_AGENT)
                .header(HttpHeaders.COOKIE, cookies)
                .retrieve()
                .toEntity(String.class);
    }
}

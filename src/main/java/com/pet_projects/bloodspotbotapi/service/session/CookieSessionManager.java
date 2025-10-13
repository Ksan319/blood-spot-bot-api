package com.pet_projects.bloodspotbotapi.service.session;

import com.pet_projects.bloodspotbotapi.client.donormos.DonorMosOnlineClient;
import com.pet_projects.bloodspotbotapi.client.donormos.dto.AuthBody;
import com.pet_projects.bloodspotbotapi.model.User;
import com.pet_projects.bloodspotbotapi.model.UserSite;
import com.pet_projects.bloodspotbotapi.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.net.HttpCookie;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CookieSessionManager {

    private final AuthService authService;
    private final DonorMosOnlineClient donorClient;


    public String getCookieHeader(User user) {
        UserSite site = user.getSite() != null ? user.getSite() : UserSite.DONOR_MOS;
        String baseUrl = site.getBaseUrl();
        String expectedLocation = site.getValidLocation();

        // Preflight: collect cookies (e.g. bpc, wordpress_test_cookie) via GET and JS redirect
        String cookies = donorClient.preflightAcquireCookies(baseUrl);

        // Perform auth with collected cookies
        ResponseEntity<String> authResp = donorClient.authWithCookies(
                AuthBody.builder()
                        .log(user.getEmail())
                        .pwd(user.getPassword())
                        .redirect_to(expectedLocation)
                        .build(),
                baseUrl,
                cookies
        );

        cookies = mergeCookieHeader(cookies, authResp);

        if (authResp.getStatusCode() == HttpStatus.FOUND
                && authResp.getHeaders().getLocation() != null
                && expectedLocation.equals(authResp.getHeaders().getLocation().toString())) {
            return cookies;
        }

        // Fallback: try to access account with cookies; if 200 — consider authorized
        ResponseEntity<String> accountResp = donorClient.getSpotsWithCookie(cookies, baseUrl);
        if (accountResp.getStatusCode().is2xxSuccessful()) {
            log.info("Account check succeeded for user {} on {}", user.getEmail(), baseUrl);
            return cookies;
        }

        String actual = authResp.getHeaders().getLocation() != null ? authResp.getHeaders().getLocation().toString() : null;
        throw new IllegalStateException(
                "Auth failed for user " + user.getEmail() +
                        ", status=" + authResp.getStatusCode() +
                        ", expectedLocation=" + expectedLocation +
                        ", actualLocation=" + actual
        );
    }

    /**
     * Из заголовков Set-Cookie формирует "name1=value1; name2=value2"
     */
    private String mergeCookieHeader(String existingCookieHeader, ResponseEntity<?> response) {
        Map<String, String> jar = new LinkedHashMap<>();
        if (existingCookieHeader != null && !existingCookieHeader.isBlank()) {
            for (String part : existingCookieHeader.split("; ")) {
                String[] kv = part.split("=", 2);
                if (kv.length == 2) jar.put(kv[0], kv[1]);
            }
        }
        List<String> setCookie = response.getHeaders().get(HttpHeaders.SET_COOKIE);
        if (setCookie != null && !setCookie.isEmpty()) {
            setCookie.stream()
                    .map(HttpCookie::parse)
                    .flatMap(List::stream)
                    .forEach(c -> jar.put(c.getName(), c.getValue()));
        }
        if (jar.isEmpty()) {
            return existingCookieHeader != null ? existingCookieHeader : "";
        }
        return jar.entrySet().stream()
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining("; "));
    }
}

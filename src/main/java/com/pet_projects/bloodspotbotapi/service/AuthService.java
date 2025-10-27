package com.pet_projects.bloodspotbotapi.service;

import com.pet_projects.bloodspotbotapi.client.donormos.DonorMosOnlineClient;
import com.pet_projects.bloodspotbotapi.client.donormos.dto.AuthBody;
import com.pet_projects.bloodspotbotapi.model.User;
import com.pet_projects.bloodspotbotapi.model.UserSite;
import com.pet_projects.bloodspotbotapi.repository.UserRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.net.HttpCookie;
import java.util.*;
import java.util.stream.Collectors;
import com.pet_projects.bloodspotbotapi.service.exception.AuthFailedException;

@Service
@RequiredArgsConstructor
@Data
@Slf4j
public class AuthService {

    private final DonorMosOnlineClient donorMosOnlineClient;
    private final UserRepository userRepository;

    @Value("${donor-source.valid-location}")
    private String validLocation; // fallback for legacy/default

    public boolean isCredentialValid(Long chatId, String username, String password) {
        UserSite site = userRepository.findById(chatId)
                .map(User::getSite)
                .orElse(UserSite.DONOR_MOS);

        String baseUrl = site != null ? site.getBaseUrl() : "https://donor-mos.online";
        String redirectTo = site != null ? site.getValidLocation() : validLocation;

        log.debug("Auth attempt: site={}, baseUrl={}, redirectTo={} (user={})",
                site, baseUrl, redirectTo, username);

        String cookies = donorMosOnlineClient.preflightAcquireCookies(baseUrl);

        ResponseEntity<String> response = donorMosOnlineClient.authWithCookies(
                AuthBody.builder()
                        .log(username)
                        .pwd(password)
                        .redirect_to(redirectTo)
                        .build(),
                baseUrl,
                cookies
        );

        cookies = mergeCookieHeader(cookies, response);

        String location = response.getHeaders().getLocation() != null
                ? response.getHeaders().getLocation().toString()
                : null;
        log.debug("Auth response: status={}, location={}, setCookieNames={}",
                response.getStatusCode(), location,
                response.getHeaders().getOrEmpty(org.springframework.http.HttpHeaders.SET_COOKIE).stream()
                        .map(h -> h.split(";", 2)[0])
                        .map(kv -> kv.split("=", 2)[0])
                        .collect(java.util.stream.Collectors.toList()));

        String expectedLocation = redirectTo;
        String actualLocation = response.getHeaders().getLocation() != null
                ? response.getHeaders().getLocation().toString()
                : null;

        // Success: explicit match to expected redirect
        if (response.getStatusCode() == HttpStatus.FOUND
                && actualLocation != null
                && actualLocation.equals(expectedLocation)) {
            return true;
        }

        // Explicit unsuccessful auth: redirected within our site but not to expected location
        if (response.getStatusCode() == HttpStatus.FOUND
                && actualLocation != null
                && actualLocation.startsWith(baseUrl)
                && !actualLocation.equals(expectedLocation)) {
            log.warn("Unsuccessful auth redirect: expected={}, actual={}", expectedLocation, actualLocation);
            return false;
        }

        // Fallback: check account page with cookies
        ResponseEntity<String> accountResp = donorMosOnlineClient.getSpotsWithCookie(cookies, baseUrl);
        // Conservative: don't treat as success unless explicit redirect matched
        return false;
    }

    public ResponseEntity<String> processAuth(String username, String password, UserSite site) {
        String redirectTo = site != null ? site.getValidLocation() : validLocation;
        String baseUrl = site != null ? site.getBaseUrl() : "https://donor-mos.online";

        log.debug("Auth attempt: site={}, baseUrl={}, redirectTo={} (user={})",
                site, baseUrl, redirectTo, username);

        String cookies = donorMosOnlineClient.preflightAcquireCookies(baseUrl);

        ResponseEntity<String> response = donorMosOnlineClient.authWithCookies(
                AuthBody.builder()
                        .log(username)
                        .pwd(password)
                        .redirect_to(redirectTo)
                        .build(),
                baseUrl,
                cookies
        );

        String location = response.getHeaders().getLocation() != null
                ? response.getHeaders().getLocation().toString()
                : null;
        List<String> cookieNames = response.getHeaders().getOrEmpty(HttpHeaders.SET_COOKIE).stream()
                .map(h -> h.split(";", 2)[0])
                .map(kv -> kv.split("=", 2)[0])
                .collect(Collectors.toList());

        log.debug("Auth response: status={}, location={}, setCookieNames={}",
                response.getStatusCode(), location, cookieNames);
        return response;
    }

    public String getCookieHeader(User user) {
        UserSite site = user.getSite() != null ? user.getSite() : UserSite.DONOR_MOS;
        String baseUrl = site.getBaseUrl();
        String expectedLocation = site.getValidLocation();

        String cookies = donorMosOnlineClient.preflightAcquireCookies(baseUrl);

        ResponseEntity<String> authResp = donorMosOnlineClient.authWithCookies(
                AuthBody.builder()
                        .log(user.getEmail())
                        .pwd(user.getPassword())
                        .redirect_to(expectedLocation)
                        .build(),
                baseUrl,
                cookies
        );

        cookies = mergeCookieHeader(cookies, authResp);

        String actual = authResp.getHeaders().getLocation() != null
                ? authResp.getHeaders().getLocation().toString()
                : null;

        // Success only if exact expected redirect
        if (authResp.getStatusCode() == HttpStatus.FOUND
                && actual != null
                && expectedLocation.equals(actual)) {
            return cookies;
        }

        // Explicit unsuccessful auth when redirected within our site but not to expected
        if (authResp.getStatusCode() == HttpStatus.FOUND
                && actual != null
                && actual.startsWith(baseUrl)
                && !expectedLocation.equals(actual)) {
            throw new AuthFailedException(
                    "Auth unsuccessful: redirected to unexpected page within site. expectedLocation="
                            + expectedLocation + ", actualLocation=" + actual);
        }

        throw new IllegalStateException(
                "Auth failed for user " + user.getEmail() +
                        ", status=" + authResp.getStatusCode() +
                        ", expectedLocation=" + expectedLocation +
                        ", actualLocation=" + actual
        );
    }

    private static String mergeCookieHeader(String existingCookieHeader, ResponseEntity<?> response) {
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

    public ResponseEntity<String> processAuth(User user) {
        UserSite site = user.getSite() != null ? user.getSite() : UserSite.DONOR_MOS;
        return processAuth(user.getEmail(), user.getPassword(), site);
    }
}

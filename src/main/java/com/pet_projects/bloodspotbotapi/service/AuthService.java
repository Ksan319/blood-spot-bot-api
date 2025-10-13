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

        log.info("Auth attempt: site={}, baseUrl={}, redirectTo={} (user={})",
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
        log.info("Auth response: status={}, location={}, setCookieNames={}",
                response.getStatusCode(), location,
                response.getHeaders().getOrEmpty(org.springframework.http.HttpHeaders.SET_COOKIE).stream()
                        .map(h -> h.split(";", 2)[0])
                        .map(kv -> kv.split("=", 2)[0])
                        .collect(java.util.stream.Collectors.toList()));

        String expectedLocation = redirectTo;
        if (response.getStatusCode() == HttpStatus.FOUND
                && response.getHeaders().getLocation() != null
                && response.getHeaders().getLocation().toString().equals(expectedLocation)) {
            return true;
        }

        // Fallback: check account page with cookies
        ResponseEntity<String> accountResp = donorMosOnlineClient.getSpotsWithCookie(cookies, baseUrl);
        return accountResp.getStatusCode().is2xxSuccessful();
    }

    public ResponseEntity<String> processAuth(String username, String password, UserSite site) {
        String redirectTo = site != null ? site.getValidLocation() : validLocation;
        String baseUrl = site != null ? site.getBaseUrl() : "https://donor-mos.online";

        log.info("Auth attempt: site={}, baseUrl={}, redirectTo={} (user={})",
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

        log.info("Auth response: status={}, location={}, setCookieNames={}",
                response.getStatusCode(), location, cookieNames);
        return response;
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

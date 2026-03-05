package com.pet_projects.bloodspotbotapi.service;

import com.pet_projects.bloodspotbotapi.client.donormos.DonorMosOnlineClient;
import com.pet_projects.bloodspotbotapi.client.donormos.dto.AuthBody;
import com.pet_projects.bloodspotbotapi.model.User;
import com.pet_projects.bloodspotbotapi.model.UserSite;
import com.pet_projects.bloodspotbotapi.repository.UserRepository;
import com.pet_projects.bloodspotbotapi.service.exception.AuthFailedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.net.HttpCookie;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

        private final DonorMosOnlineClient client;
        private final UserRepository userRepository;

        @Value("${donor-source.valid-location}")
        private String validLocation;

        public boolean isCredentialValid(Long chatId, String username, String password) {
                try {
                        UserSite site = userRepository.findById(chatId)
                                        .map(User::getSite)
                                        .orElse(UserSite.DONOR_MOS);
                        getCookieHeader(username, password, site);
                        return true;
                } catch (Exception e) {
                        log.warn("Auth failed for {}: {}", username, e.getMessage());
                        return false;
                }
        }

        public String getCookieHeader(User user) {
                UserSite site = user.getSite() != null ? user.getSite() : UserSite.DONOR_MOS;
                return getCookieHeader(user.getEmail(), user.getPassword(), site);
        }

        private String getCookieHeader(String email, String password, UserSite site) {
                String baseUrl = site != null ? site.getBaseUrl() : "https://donor-mos.online";
                String redirectTo = site != null ? site.getValidLocation() : validLocation;

                // 1. Preflight: collect cookies required by the site before auth
                Map<String, String> cookieJar = preflightCollectCookies(baseUrl);

                // 2. POST auth with preflight cookies
                ResponseEntity<String> authResp = client.auth(
                                AuthBody.builder()
                                                .log(email)
                                                .pwd(password)
                                                .redirect_to(redirectTo)
                                                .build(),
                                baseUrl,
                                buildCookieHeader(cookieJar));

                // 3. Merge auth response cookies
                collectSetCookies(cookieJar, authResp);
                extractBodyCookies(cookieJar, authResp);

                String cookies = buildCookieHeader(cookieJar);
                if (cookies.isEmpty()) {
                        throw new AuthFailedException("Failed to extract cookies for user " + email);
                }

                // 4. Verify: GET /account/ and check for expected element
                ResponseEntity<String> accountResp = client.getAccountPage(baseUrl, cookies);
                String body = accountResp.getBody();
                if (body != null && body.contains("table-item__date")) {
                        return cookies;
                }

                throw new AuthFailedException(
                                "Auth failed for user " + email + ", account page does not contain expected elements.");
        }

        // --- Preflight ---

        private Map<String, String> preflightCollectCookies(String baseUrl) {
                Map<String, String> jar = new LinkedHashMap<>();

                // First GET /auth.php
                ResponseEntity<String> first = client.getLoginPage(baseUrl, null);
                collectSetCookies(jar, first);

                // Parse JS cookie / redirect from body
                String body = first.getBody() != null ? first.getBody() : "";
                String jsCookie = extractJsCookieFromHtml(body);
                String jsRedirect = extractJsRedirectFromHtml(body);

                if (jsCookie != null) {
                        putKeyValue(jar, jsCookie);
                }
                if (jsRedirect != null) {
                        ResponseEntity<String> redirectResp = client.getAbsoluteUrl(jsRedirect, buildCookieHeader(jar));
                        collectSetCookies(jar, redirectResp);
                }

                // Second GET /auth.php with collected cookies (gets wordpress_test_cookie etc.)
                ResponseEntity<String> second = client.getLoginPage(baseUrl, buildCookieHeader(jar));
                collectSetCookies(jar, second);

                log.debug("Preflight cookies for {}: {}", baseUrl, jar.keySet());
                return jar;
        }

        // --- Cookie utils ---

        private static void collectSetCookies(Map<String, String> jar, ResponseEntity<?> response) {
                List<String> setCookie = response.getHeaders().get(HttpHeaders.SET_COOKIE);
                if (setCookie == null || setCookie.isEmpty())
                        return;
                setCookie.stream()
                                .map(HttpCookie::parse)
                                .flatMap(List::stream)
                                .forEach(c -> jar.put(c.getName(), c.getValue()));
        }

        private static void extractBodyCookies(Map<String, String> jar, ResponseEntity<String> response) {
                if (!response.getStatusCode().is2xxSuccessful())
                        return;
                String body = response.getBody();
                if (body == null)
                        return;
                String kv = extractJsCookieFromHtml(body);
                if (kv != null) {
                        putKeyValue(jar, kv);
                }
        }

        private static void putKeyValue(Map<String, String> jar, String kv) {
                String[] parts = kv.split("=", 2);
                if (parts.length == 2 && !parts[0].isBlank()) {
                        jar.put(parts[0], parts[1]);
                }
        }

        private static String buildCookieHeader(Map<String, String> jar) {
                if (jar.isEmpty())
                        return "";
                return jar.entrySet().stream()
                                .map(e -> e.getKey() + "=" + e.getValue())
                                .collect(Collectors.joining("; "));
        }

        // --- HTML parsing ---

        private static String extractJsCookieFromHtml(String html) {
                Pattern p = Pattern.compile("document\\.cookie\\s*=\\s*\"([^\"]+)\"");
                Matcher m = p.matcher(html);
                if (m.find()) {
                        String full = m.group(1);
                        int idx = full.indexOf(';');
                        return idx > 0 ? full.substring(0, idx) : full;
                }
                return null;
        }

        private static String extractJsRedirectFromHtml(String html) {
                Pattern p = Pattern.compile("document\\.location\\.href\\s*=\\s*\"([^\"]+)\"");
                Matcher m = p.matcher(html);
                return m.find() ? m.group(1) : null;
        }
}

package com.pet_projects.bloodspotbotapi.service;

import com.pet_projects.bloodspotbotapi.client.donormos.DonorMosOnlineClient;
import com.pet_projects.bloodspotbotapi.client.donormos.dto.AuthBody;
import com.pet_projects.bloodspotbotapi.config.AuthRetryProperties;
import com.pet_projects.bloodspotbotapi.config.EncryptionProperties;
import com.pet_projects.bloodspotbotapi.model.User;
import com.pet_projects.bloodspotbotapi.model.UserSite;
import com.pet_projects.bloodspotbotapi.repository.UserRepository;
import com.pet_projects.bloodspotbotapi.service.exception.AuthFailedException;
import com.pet_projects.bloodspotbotapi.service.exception.SiteUnavailableException;
import com.pet_projects.bloodspotbotapi.utils.EncryptionUtils;
import com.pet_projects.bloodspotbotapi.utils.HtmlUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

import java.net.HttpCookie;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

        private final DonorMosOnlineClient client;
        private final UserRepository userRepository;
        private final EncryptionProperties encryptionProperties;
        private final AuthRetryProperties retryProperties;

        public boolean isCredentialValid(Long chatId, String username, String password) {
                UserSite resolvedSite = userRepository.findById(chatId)
                                .map(User::getSite)
                                .orElse(UserSite.DONOR_MOS);
                if (resolvedSite.isAll()) {
                        resolvedSite = UserSite.DONOR_MOS;
                }
                final UserSite site = resolvedSite;
                final String siteName = site.getDisplayName();
                try {
                        executeWithRetry(() -> getCookieHeader(username, password, site), siteName);
                        return true;
                } catch (SiteUnavailableException e) {
                        log.warn("Site {} is unavailable after {} attempts for user {}",
                                        siteName, retryProperties.getMaxAttempts(), username);
                        throw e;
                } catch (AuthFailedException e) {
                        log.warn("Auth failed for {}: {}", username, e.getMessage());
                        return false;
                }
        }

        public String getCookieHeader(User user) {
                return getCookieHeader(user, user.getSite());
        }

        public String getCookieHeader(User user, UserSite site) {
                String decryptedPassword;
                try {
                        decryptedPassword = EncryptionUtils.decrypt(user.getPassword(), encryptionProperties.getSecretKey());
                } catch (EncryptionUtils.EncryptionException e) {
                        log.error("Failed to decrypt password for user {}: {}", user.getId(), e.getMessage());
                        throw new AuthFailedException("Failed to decrypt user credentials", e);
                }
                return getCookieHeader(user.getEmail(), decryptedPassword, site);
        }

        private <T> T executeWithRetry(Supplier<T> action, String siteName) {
                int attempts = retryProperties.getMaxAttempts();
                int delayMs = retryProperties.getDelayMs();
                RestClientException lastException = null;

                for (int i = 0; i < attempts; i++) {
                        try {
                                return action.get();
                        } catch (RestClientException e) {
                                lastException = e;
                                log.debug("Attempt {}/{} failed for site {}: {}", i + 1, attempts, siteName, e.getMessage());
                                if (i < attempts - 1) {
                                        try {
                                                Thread.sleep(delayMs);
                                        } catch (InterruptedException ie) {
                                                Thread.currentThread().interrupt();
                                                break;
                                        }
                                }
                        } catch (AuthFailedException e) {
                                throw e;
                        }
                }

                throw new SiteUnavailableException(siteName, lastException);
        }

        private String getCookieHeader(String email, String password, UserSite site) {
                String baseUrl = site.getBaseUrl();
                String redirectTo = site.getValidLocation();

                Map<String, String> cookieJar = preflightCollectCookies(baseUrl);

                ResponseEntity<String> authResp = client.auth(
                                AuthBody.builder()
                                                .log(email)
                                                .pwd(password)
                                                .redirect_to(redirectTo)
                                                .build(),
                                baseUrl,
                                buildCookieHeader(cookieJar));

                collectSetCookies(cookieJar, authResp);
                extractBodyCookies(cookieJar, authResp);

                String cookies = buildCookieHeader(cookieJar);
                if (cookies.isEmpty()) {
                        throw new AuthFailedException("Failed to extract cookies for user " + email);
                }

                ResponseEntity<String> accountResp = client.getAccountPage(baseUrl, cookies);
                String body = accountResp.getBody();
                if (body != null && body.contains("table-item__date")) {
                        return cookies;
                }

                throw new SiteUnavailableException(
                                "Account page for " + site.getDisplayName() + " does not contain expected elements. Site may be unavailable or slow to respond.");
        }

        private Map<String, String> preflightCollectCookies(String baseUrl) {
                Map<String, String> jar = new LinkedHashMap<>();

                ResponseEntity<String> first = client.getLoginPage(baseUrl, null);
                collectSetCookies(jar, first);

                String body = first.getBody() != null ? first.getBody() : "";
                String jsCookie = HtmlUtils.extractJsCookieFromHtml(body);
                String jsRedirect = HtmlUtils.extractJsRedirectFromHtml(body);

                if (jsCookie != null) {
                        putKeyValue(jar, jsCookie);
                }
                if (jsRedirect != null) {
                        ResponseEntity<String> redirectResp = client.getAbsoluteUrl(jsRedirect, buildCookieHeader(jar));
                        collectSetCookies(jar, redirectResp);
                }

                ResponseEntity<String> second = client.getLoginPage(baseUrl, buildCookieHeader(jar));
                collectSetCookies(jar, second);

                log.debug("Preflight cookies for {}: {}", baseUrl, jar.keySet());
                return jar;
        }

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
                String kv = HtmlUtils.extractJsCookieFromHtml(body);
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
}

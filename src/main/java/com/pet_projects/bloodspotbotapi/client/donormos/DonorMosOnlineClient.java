package com.pet_projects.bloodspotbotapi.client.donormos;

import com.pet_projects.bloodspotbotapi.client.donormos.dto.AuthBody;
import com.pet_projects.bloodspotbotapi.utils.FormUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.HttpCookie;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@Slf4j
public class DonorMosOnlineClient {

    private RestClient buildClient(String baseUrl) {
        return RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/125.0.0.0 Safari/537.36";

    public ResponseEntity<String> auth(AuthBody authBody, String baseUrl) {
        MultiValueMap<String, String> form = FormUtils.toFormData(authBody);

        return buildClient(baseUrl)
                .post()
                .uri("/auth.php")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .toEntity(String.class);
    }

    public ResponseEntity<String> getSpotsWithCookie(String cookieHeader, String baseUrl) {
        return buildClient(baseUrl)
                .get()
                .uri("/account/")
                .header(HttpHeaders.COOKIE, cookieHeader)
                .retrieve()
                .toEntity(String.class);
    }

    public ResponseEntity<String> getLoginPage(String baseUrl, String cookieHeader) {
        RestClient.RequestHeadersSpec<?> spec = buildClient(baseUrl)
                .get()
                .uri("/auth.php")
                .header(HttpHeaders.USER_AGENT, USER_AGENT)
                .header(HttpHeaders.ACCEPT, "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8")
                .header(HttpHeaders.ACCEPT_LANGUAGE, "ru-RU,ru;q=0.9,en-US;q=0.8,en;q=0.7");
        if (cookieHeader != null && !cookieHeader.isBlank()) {
            spec = spec.header(HttpHeaders.COOKIE, cookieHeader);
        }
        return spec.retrieve().toEntity(String.class);
    }

    public ResponseEntity<String> authWithCookies(AuthBody authBody, String baseUrl, String cookieHeader) {
        MultiValueMap<String, String> form = FormUtils.toFormData(authBody);
        return buildClient(baseUrl)
                .post()
                .uri("/auth.php")
                .header(HttpHeaders.USER_AGENT, USER_AGENT)
                .header(HttpHeaders.REFERER, UriComponentsBuilder.fromHttpUrl(baseUrl).path("/auth.php").toUriString())
                .header(HttpHeaders.COOKIE, cookieHeader)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .toEntity(String.class);
    }

    public ResponseEntity<String> getAbsolute(String absoluteUrl, String cookieHeader) {
        RestClient client = RestClient.builder().build();
        RestClient.RequestHeadersSpec<?> spec = client.get()
                .uri(absoluteUrl)
                .header(HttpHeaders.USER_AGENT, USER_AGENT);
        if (cookieHeader != null && !cookieHeader.isBlank()) {
            spec = spec.header(HttpHeaders.COOKIE, cookieHeader);
        }
        return spec.retrieve().toEntity(String.class);
    }

    public String preflightAcquireCookies(String baseUrl) {
        // 1) Initial GET /auth.php
        ResponseEntity<String> first = getLoginPage(baseUrl, null);
        Map<String, String> cookies = new LinkedHashMap<>();
        collectCookies(cookies, first);

        // 1.1) Parse JS cookie and redirect if present
        String body = first.getBody() != null ? first.getBody() : "";
        String jsCookie = extractJsCookie(body); // returns name=value or null
        String nextHref = extractJsRedirect(body); // absolute URL or null
        if (jsCookie != null) {
            addCookieKV(cookies, jsCookie);
        }
        if (nextHref != null) {
            ResponseEntity<String> next = getAbsolute(nextHref, buildCookieHeader(cookies));
            collectCookies(cookies, next);
        }

        // 2) Second GET /auth.php with cookies to get wordpress_test_cookie
        ResponseEntity<String> second = getLoginPage(baseUrl, buildCookieHeader(cookies));
        collectCookies(cookies, second);

        String header = buildCookieHeader(cookies);
        log.info("Preflight cookies collected for baseUrl={}: {}", baseUrl, cookies.keySet());
        return header;
    }

    private static void collectCookies(Map<String, String> jar, ResponseEntity<?> response) {
        List<String> setCookie = response.getHeaders().get(HttpHeaders.SET_COOKIE);
        if (setCookie == null || setCookie.isEmpty()) return;
        setCookie.stream()
                .map(HttpCookie::parse)
                .flatMap(List::stream)
                .forEach(c -> jar.put(c.getName(), c.getValue()));
    }

    private static void addCookieKV(Map<String, String> jar, String kv) {
        String[] parts = kv.split("=", 2);
        if (parts.length == 2 && !parts[0].isBlank()) {
            jar.put(parts[0], parts[1]);
        }
    }

    private static String buildCookieHeader(Map<String, String> jar) {
        return jar.entrySet().stream()
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(java.util.stream.Collectors.joining("; "));
    }

    private static String extractJsCookie(String html) {
        // looks for: document.cookie="name=value;..."; take the first name=value before semicolon
        Pattern p = Pattern.compile("document\\.cookie\\s*=\\s*\"([^\"]+)\"");
        Matcher m = p.matcher(html);
        if (m.find()) {
            String full = m.group(1);
            int idx = full.indexOf(';');
            return idx > 0 ? full.substring(0, idx) : full;
        }
        return null;
    }

    private static String extractJsRedirect(String html) {
        Pattern p = Pattern.compile("document\\.location\\.href\\s*=\\s*\"([^\"]+)\"");
        Matcher m = p.matcher(html);
        if (m.find()) {
            return m.group(1);
        }
        return null;
    }
}

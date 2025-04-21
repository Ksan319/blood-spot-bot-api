package com.pet_projects.bloodspotbotapi.service.session;

import com.pet_projects.bloodspotbotapi.client.donormos.dto.AuthBody;
import com.pet_projects.bloodspotbotapi.service.AuthService;
import com.pet_projects.bloodspotbotapi.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.net.HttpCookie;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CookieSessionManager {

    private final AuthService authService;


    public String getCookieHeader(User user) {
        ResponseEntity<String> authResp = authService.processAuth(
                user.getEmail(),
                user.getPassword()
        );

        if (authResp.getStatusCode() == HttpStatus.FOUND
                && authResp.getHeaders().getLocation() != null
                && authResp.getHeaders().getLocation().toString().equals(authService.getValidLocation())) {

            return extractCookieHeader(authResp);
        }

        throw new IllegalStateException(
                "Auth failed for user " + user.getEmail() + ", status=" + authResp.getStatusCode()
        );
    }

    /**
     * Из заголовков Set-Cookie формирует "name1=value1; name2=value2"
     */
    private String extractCookieHeader(ResponseEntity<?> response) {
        List<String> setCookie = response.getHeaders().get(HttpHeaders.SET_COOKIE);
        if (setCookie == null || setCookie.isEmpty()) {
            throw new IllegalStateException("No Set-Cookie headers present");
        }

        return setCookie.stream()
                .map(HttpCookie::parse)         // List<HttpCookie>
                .flatMap(List::stream)          // каждый cookie
                .map(c -> c.getName() + "=" + c.getValue())
                .collect(Collectors.joining("; "));
    }
}

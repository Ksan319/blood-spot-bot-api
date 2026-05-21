package com.pet_projects.bloodspotbotapi.service;

import com.pet_projects.bloodspotbotapi.config.ErrorLogProperties;
import com.pet_projects.bloodspotbotapi.model.SiteError;
import com.pet_projects.bloodspotbotapi.model.User;
import com.pet_projects.bloodspotbotapi.model.UserSite;
import com.pet_projects.bloodspotbotapi.repository.SiteErrorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class SiteErrorService {

    private final SiteErrorRepository siteErrorRepository;
    private final ErrorLogProperties errorLogProperties;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public void logSiteUnavailable(User user, UserSite site, String errorMessage) {
        SiteError error = SiteError.builder()
                .userId(user.getId())
                .userEmail(user.getEmail())
                .site(site)
                .errorType("SITE_UNAVAILABLE")
                .errorMessage(truncate(errorMessage, 1024))
                .createdAt(LocalDateTime.now())
                .build();
        siteErrorRepository.save(error);
        log.warn("Site error logged: userId={}, site={}, message={}", user.getId(), site, errorMessage);
    }

    public void logAuthFailed(User user, UserSite site, String errorMessage) {
        SiteError error = SiteError.builder()
                .userId(user.getId())
                .userEmail(user.getEmail())
                .site(site)
                .errorType("AUTH_FAILED")
                .errorMessage(truncate(errorMessage, 1024))
                .createdAt(LocalDateTime.now())
                .build();
        siteErrorRepository.save(error);
        log.warn("Auth error logged: userId={}, site={}, message={}", user.getId(), site, errorMessage);
    }

    public String formatLastErrors() {
        List<SiteError> errors = siteErrorRepository.findLastErrors(
                PageRequest.of(0, errorLogProperties.getLastCount()));
        
        if (errors.isEmpty()) {
            return "Нет записанных ошибок.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Последние ошибки (").append(errors.size()).append("):\n\n");
        for (int i = 0; i < errors.size(); i++) {
            SiteError e = errors.get(i);
            sb.append(i + 1).append(". ");
            sb.append(e.getCreatedAt().format(FORMATTER));
            sb.append(" | ").append(e.getSite().getDisplayName());
            sb.append(" | ").append(e.getErrorType());
            sb.append("\n   Email: ").append(e.getUserEmail() != null ? e.getUserEmail() : "N/A");
            sb.append("\n   Ошибка: ").append(e.getErrorMessage());
            sb.append("\n\n");
        }
        return sb.toString().trim();
    }

    private static String truncate(String str, int maxLen) {
        if (str == null) return null;
        return str.length() > maxLen ? str.substring(0, maxLen) : str;
    }
}

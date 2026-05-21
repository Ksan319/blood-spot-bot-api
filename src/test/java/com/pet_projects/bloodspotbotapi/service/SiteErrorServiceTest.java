package com.pet_projects.bloodspotbotapi.service;

import com.pet_projects.bloodspotbotapi.config.ErrorLogProperties;
import com.pet_projects.bloodspotbotapi.model.SiteError;
import com.pet_projects.bloodspotbotapi.model.User;
import com.pet_projects.bloodspotbotapi.model.UserSite;
import com.pet_projects.bloodspotbotapi.repository.SiteErrorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SiteErrorServiceTest {

    private SiteErrorService siteErrorService;

    @Mock
    private SiteErrorRepository siteErrorRepository;

    private ErrorLogProperties errorLogProperties;

    @BeforeEach
    public void setUp() {
        errorLogProperties = new ErrorLogProperties();
        errorLogProperties.setLastCount(15);
        siteErrorService = new SiteErrorService(siteErrorRepository, errorLogProperties);
    }

    @Test
    public void testLogSiteUnavailable_SavesError() {
        User user = User.builder().id(123L).email("test@example.com").build();
        UserSite site = UserSite.DONOR_MOS;
        String errorMessage = "Site is slow to respond";

        siteErrorService.logSiteUnavailable(user, site, errorMessage);

        ArgumentCaptor<SiteError> captor = ArgumentCaptor.forClass(SiteError.class);
        verify(siteErrorRepository).save(captor.capture());

        SiteError saved = captor.getValue();
        assertEquals(123L, saved.getUserId());
        assertEquals("test@example.com", saved.getUserEmail());
        assertEquals(UserSite.DONOR_MOS, saved.getSite());
        assertEquals("SITE_UNAVAILABLE", saved.getErrorType());
        assertEquals(errorMessage, saved.getErrorMessage());
        assertNotNull(saved.getCreatedAt());
    }

    @Test
    public void testLogAuthFailed_SavesError() {
        User user = User.builder().id(456L).email("user@example.com").build();
        UserSite site = UserSite.DONOR_MOS_SAB;
        String errorMessage = "Invalid credentials";

        siteErrorService.logAuthFailed(user, site, errorMessage);

        ArgumentCaptor<SiteError> captor = ArgumentCaptor.forClass(SiteError.class);
        verify(siteErrorRepository).save(captor.capture());

        SiteError saved = captor.getValue();
        assertEquals(456L, saved.getUserId());
        assertEquals("user@example.com", saved.getUserEmail());
        assertEquals(UserSite.DONOR_MOS_SAB, saved.getSite());
        assertEquals("AUTH_FAILED", saved.getErrorType());
        assertEquals(errorMessage, saved.getErrorMessage());
    }

    @Test
    public void testLogSiteUnavailable_TruncatesLongMessage() {
        User user = User.builder().id(123L).email("test@example.com").build();
        String longMessage = "x".repeat(2000);

        siteErrorService.logSiteUnavailable(user, UserSite.DONOR_MOS_ZAR, longMessage);

        ArgumentCaptor<SiteError> captor = ArgumentCaptor.forClass(SiteError.class);
        verify(siteErrorRepository).save(captor.capture());

        assertEquals(1024, captor.getValue().getErrorMessage().length());
    }

    @Test
    public void testFormatLastErrors_EmptyList() {
        when(siteErrorRepository.findLastErrors(any(Pageable.class))).thenReturn(List.of());

        String result = siteErrorService.formatLastErrors();

        assertEquals("Нет записанных ошибок.", result);
    }

    @Test
    public void testFormatLastErrors_WithErrors() {
        LocalDateTime now = LocalDateTime.of(2026, 5, 21, 10, 30);
        SiteError error1 = SiteError.builder()
                .id(1L)
                .userId(100L)
                .userEmail("user1@example.com")
                .site(UserSite.DONOR_MOS)
                .errorType("SITE_UNAVAILABLE")
                .errorMessage("Connection timeout")
                .createdAt(now)
                .build();
        SiteError error2 = SiteError.builder()
                .id(2L)
                .userId(200L)
                .userEmail("user2@example.com")
                .site(UserSite.DONOR_MOS_SAB)
                .errorType("AUTH_FAILED")
                .errorMessage("Invalid password")
                .createdAt(now.minusMinutes(5))
                .build();

        when(siteErrorRepository.findLastErrors(any(Pageable.class))).thenReturn(List.of(error1, error2));

        String result = siteErrorService.formatLastErrors();

        assertTrue(result.contains("Последние ошибки (2):"));
        assertTrue(result.contains("1. 2026-05-21 10:30"));
        assertTrue(result.contains("Поликарпова"));
        assertTrue(result.contains("SITE_UNAVAILABLE"));
        assertTrue(result.contains("user1@example.com"));
        assertTrue(result.contains("Connection timeout"));
        assertTrue(result.contains("2. 2026-05-21 10:25"));
        assertTrue(result.contains("Шаболовка"));
        assertTrue(result.contains("AUTH_FAILED"));
        assertTrue(result.contains("user2@example.com"));
        assertTrue(result.contains("Invalid password"));
    }

    @Test
    public void testFormatLastErrors_WithNullEmail() {
        LocalDateTime now = LocalDateTime.of(2026, 5, 21, 10, 0);
        SiteError error = SiteError.builder()
                .id(1L)
                .userId(100L)
                .userEmail(null)
                .site(UserSite.DONOR_MOS_ZAR)
                .errorType("SITE_UNAVAILABLE")
                .errorMessage("Site down")
                .createdAt(now)
                .build();

        when(siteErrorRepository.findLastErrors(any(Pageable.class))).thenReturn(List.of(error));

        String result = siteErrorService.formatLastErrors();

        assertTrue(result.contains("Email: N/A"));
    }
}

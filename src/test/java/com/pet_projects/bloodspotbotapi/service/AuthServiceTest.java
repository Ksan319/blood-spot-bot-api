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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import org.springframework.http.HttpHeaders;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class AuthServiceTest {

    private static final String TEST_SECRET_KEY = "test-secret-key-32-characters!!!";

    private AuthService authService;

    @Mock
    private DonorMosOnlineClient client;

    @Mock
    private UserRepository userRepository;

    private EncryptionProperties encryptionProperties;
    private AuthRetryProperties retryProperties;

    private ResponseEntity<String> loginPageResp;
    private ResponseEntity<String> authResp;
    private ResponseEntity<String> accountResp;
    private ResponseEntity<String> accountRespNoDate;

    @BeforeEach
    void setUp() {
        encryptionProperties = new EncryptionProperties();
        encryptionProperties.setSecretKey(TEST_SECRET_KEY);
        retryProperties = new AuthRetryProperties();
        retryProperties.setMaxAttempts(3);
        retryProperties.setDelayMs(10);
        authService = new AuthService(client, userRepository, encryptionProperties, retryProperties);

        HttpHeaders loginHeaders = new HttpHeaders();
        loginHeaders.add(HttpHeaders.SET_COOKIE, "wordpress_test_cookie=ok; Path=/");

        loginPageResp = ResponseEntity.ok()
                .headers(loginHeaders)
                .body("<html><body>OK</body></html>");

        HttpHeaders authHeaders = new HttpHeaders();
        authHeaders.add(HttpHeaders.SET_COOKIE, "session=abc123; Path=/");

        authResp = ResponseEntity.ok()
                .headers(authHeaders)
                .body("<html>Logged in</html>");

        accountResp = ResponseEntity.ok("<div class=\"table-item__date\">01.01.2025</div>");
        accountRespNoDate = ResponseEntity.ok("<html><body>No dates here</body></html>");

        when(userRepository.findById(any())).thenReturn(Optional.empty());
    }

    @Test
    void testIsCredentialValid_SiteUnavailableAfterRetries_ThrowsSiteUnavailableException() {
        Long chatId = 123L;

        when(client.getLoginPage(anyString(), any()))
                .thenThrow(new RestClientException("Connection refused"));

        SiteUnavailableException ex = assertThrows(SiteUnavailableException.class,
                () -> authService.isCredentialValid(chatId, "user@test.com", "password"));

        assertEquals("Поликарпова", ex.getSiteName());
        verify(client, times(3)).getLoginPage(anyString(), any());
    }

    @Test
    void testIsCredentialValid_AuthFailedException_NotRetried() {
        Long chatId = 123L;

        when(client.getLoginPage(anyString(), any())).thenReturn(loginPageResp);
        when(client.getAbsoluteUrl(anyString(), any())).thenReturn(loginPageResp);
        when(client.auth(any(AuthBody.class), anyString(), anyString()))
                .thenThrow(new AuthFailedException("Bad credentials"));

        boolean result = authService.isCredentialValid(chatId, "user@test.com", "password");

        assertFalse(result);
        verify(client, times(1)).auth(any(AuthBody.class), anyString(), anyString());
    }

    @Test
    void testIsCredentialValid_Success() {
        Long chatId = 123L;

        when(client.getLoginPage(anyString(), any())).thenReturn(loginPageResp);
        when(client.getAbsoluteUrl(anyString(), any())).thenReturn(loginPageResp);
        when(client.auth(any(AuthBody.class), anyString(), anyString())).thenReturn(authResp);
        when(client.getAccountPage(anyString(), anyString())).thenReturn(accountResp);

        boolean result = authService.isCredentialValid(chatId, "user@test.com", "password");

        assertTrue(result);
    }

    @Test
    void testIsCredentialValid_MissingTableItemDate_ReturnsFalse() {
        Long chatId = 123L;

        when(client.getLoginPage(anyString(), any())).thenReturn(loginPageResp);
        when(client.getAbsoluteUrl(anyString(), any())).thenReturn(loginPageResp);
        when(client.auth(any(AuthBody.class), anyString(), anyString())).thenReturn(authResp);
        when(client.getAccountPage(anyString(), anyString())).thenReturn(accountRespNoDate);

        boolean result = authService.isCredentialValid(chatId, "user@test.com", "password");

        assertFalse(result);
    }

    @Test
    void testIsCredentialValid_AllSites_SuccessOnFirst() {
        Long chatId = 123L;
        User user = User.builder().id(chatId).site(UserSite.ALL).build();
        when(userRepository.findById(chatId)).thenReturn(Optional.of(user));

        when(client.getLoginPage(anyString(), any())).thenReturn(loginPageResp);
        when(client.getAbsoluteUrl(anyString(), any())).thenReturn(loginPageResp);
        when(client.auth(any(AuthBody.class), anyString(), anyString())).thenReturn(authResp);
        when(client.getAccountPage(anyString(), anyString())).thenReturn(accountResp);

        boolean result = authService.isCredentialValid(chatId, "user@test.com", "password");

        assertTrue(result);
        // Should stop after first success, so only 1 site checked
        verify(client, times(1)).getAccountPage(anyString(), anyString());
    }

    @Test
    void testIsCredentialValid_AllSites_AllUnavailable_ThrowsSiteUnavailableException() {
        Long chatId = 123L;
        User user = User.builder().id(chatId).site(UserSite.ALL).build();
        when(userRepository.findById(chatId)).thenReturn(Optional.of(user));

        when(client.getLoginPage(anyString(), any()))
                .thenThrow(new RestClientException("Connection refused"));

        SiteUnavailableException ex = assertThrows(SiteUnavailableException.class,
                () -> authService.isCredentialValid(chatId, "user@test.com", "password"));

        assertEquals("Все медцентры", ex.getSiteName());
        // 3 sites × 3 retries = 9 calls
        verify(client, times(9)).getLoginPage(anyString(), any());
    }

    @Test
    void testIsCredentialValid_AllSites_AllAuthFailed_ReturnsFalse() {
        Long chatId = 123L;
        User user = User.builder().id(chatId).site(UserSite.ALL).build();
        when(userRepository.findById(chatId)).thenReturn(Optional.of(user));

        when(client.getLoginPage(anyString(), any())).thenReturn(loginPageResp);
        when(client.getAbsoluteUrl(anyString(), any())).thenReturn(loginPageResp);
        when(client.auth(any(AuthBody.class), anyString(), anyString()))
                .thenThrow(new AuthFailedException("Bad credentials"));

        boolean result = authService.isCredentialValid(chatId, "user@test.com", "password");

        assertFalse(result);
        // All 3 sites checked
        verify(client, times(3)).auth(any(AuthBody.class), anyString(), anyString());
    }

    @Test
    void testIsCredentialValid_AllSites_MixedUnavailableAndAuthFailed_ReturnsFalse() {
        Long chatId = 123L;
        User user = User.builder().id(chatId).site(UserSite.ALL).build();
        when(userRepository.findById(chatId)).thenReturn(Optional.of(user));

        // First site: unavailable
        when(client.getLoginPage(eq(UserSite.DONOR_MOS.getBaseUrl()), any()))
                .thenThrow(new RestClientException("Connection refused"));
        // Second site: auth failed
        when(client.getLoginPage(eq(UserSite.DONOR_MOS_SAB.getBaseUrl()), any())).thenReturn(loginPageResp);
        when(client.getAbsoluteUrl(anyString(), any())).thenReturn(loginPageResp);
        when(client.auth(any(AuthBody.class), eq(UserSite.DONOR_MOS_SAB.getBaseUrl()), anyString()))
                .thenThrow(new AuthFailedException("Bad credentials"));
        // Third site: auth failed
        when(client.getLoginPage(eq(UserSite.DONOR_MOS_ZAR.getBaseUrl()), any())).thenReturn(loginPageResp);
        when(client.auth(any(AuthBody.class), eq(UserSite.DONOR_MOS_ZAR.getBaseUrl()), anyString()))
                .thenThrow(new AuthFailedException("Bad credentials"));

        boolean result = authService.isCredentialValid(chatId, "user@test.com", "password");

        assertFalse(result);
    }

    @Test
    void testIsCredentialValid_AllSites_MixedUnavailableAndSuccess_ReturnsTrue() {
        Long chatId = 123L;
        User user = User.builder().id(chatId).site(UserSite.ALL).build();
        when(userRepository.findById(chatId)).thenReturn(Optional.of(user));

        // First site: unavailable
        when(client.getLoginPage(eq(UserSite.DONOR_MOS.getBaseUrl()), any()))
                .thenThrow(new RestClientException("Connection refused"));
        // Second site: success
        when(client.getLoginPage(eq(UserSite.DONOR_MOS_SAB.getBaseUrl()), any())).thenReturn(loginPageResp);
        when(client.getAbsoluteUrl(anyString(), any())).thenReturn(loginPageResp);
        when(client.auth(any(AuthBody.class), eq(UserSite.DONOR_MOS_SAB.getBaseUrl()), anyString())).thenReturn(authResp);
        when(client.getAccountPage(eq(UserSite.DONOR_MOS_SAB.getBaseUrl()), anyString())).thenReturn(accountResp);

        boolean result = authService.isCredentialValid(chatId, "user@test.com", "password");

        assertTrue(result);
    }

}

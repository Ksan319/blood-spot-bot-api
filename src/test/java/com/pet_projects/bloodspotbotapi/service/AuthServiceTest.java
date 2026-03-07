package com.pet_projects.bloodspotbotapi.service;

import com.pet_projects.bloodspotbotapi.client.donormos.DonorMosOnlineClient;
import com.pet_projects.bloodspotbotapi.model.User;
import com.pet_projects.bloodspotbotapi.model.UserSite;
import com.pet_projects.bloodspotbotapi.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    private AuthService authService;

    @Mock
    private UserRepository userRepository;

    @BeforeEach
    public void setUp() {
        DonorMosOnlineClient client = new DonorMosOnlineClient();
        authService = new AuthService(client, userRepository);
    }

    @ParameterizedTest
    @EnumSource(UserSite.class)
    public void testAuthValidCredentials(UserSite site) {
        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword("test");
        user.setSite(site);

        String cookies = authService.getCookieHeader(user);

        assertNotNull(cookies, "Cookies should not be null for site " + site);
        assertTrue(!cookies.isEmpty(), "Cookies should not be empty for site " + site);
    }
}

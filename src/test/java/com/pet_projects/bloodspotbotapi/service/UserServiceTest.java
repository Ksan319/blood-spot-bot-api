package com.pet_projects.bloodspotbotapi.service;

import com.pet_projects.bloodspotbotapi.model.User;
import com.pet_projects.bloodspotbotapi.model.UserSite;
import com.pet_projects.bloodspotbotapi.repository.SpotRepository;
import com.pet_projects.bloodspotbotapi.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SpotRepository spotRepository;

    @BeforeEach
    public void setUp() {
        userService = new UserService(userRepository, spotRepository);
    }

    @Test
    public void testSaveOrUpdate_NewUser() {
        Long chatId = 123L;
        String email = "test@example.com";
        String password = "password";

        when(userRepository.findById(chatId)).thenReturn(Optional.empty());

        userService.saveOrUpdate(chatId, email, password);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertEquals(chatId, savedUser.getId());
        assertEquals(email, savedUser.getEmail());
        assertEquals(password, savedUser.getPassword());
        assertTrue(savedUser.isActive());
        assertEquals(UserSite.DONOR_MOS, savedUser.getSite());
    }

    @Test
    public void testSaveOrUpdate_ExistingUser() {
        Long chatId = 123L;
        String email = "updated@example.com";
        String password = "new_password";
        User existingUser = User.builder().id(chatId).site(UserSite.DONOR_MOS_SAB).build();

        when(userRepository.findById(chatId)).thenReturn(Optional.of(existingUser));

        userService.saveOrUpdate(chatId, email, password);

        verify(userRepository).save(existingUser);
        assertEquals(email, existingUser.getEmail());
        assertEquals(password, existingUser.getPassword());
        assertTrue(existingUser.isActive());
        assertEquals(UserSite.DONOR_MOS_SAB, existingUser.getSite());
    }

    @Test
    public void testChangeSubscription_ToggleOn() {
        Long chatId = 123L;
        User user = User.builder().id(chatId).subscribed(false).build();

        when(userRepository.findById(chatId)).thenReturn(Optional.of(user));

        userService.changeSubscription(chatId);

        assertTrue(user.isSubscribed());
        verify(userRepository).save(user);
    }

    @Test
    public void testChangeSubscription_ToggleOff() {
        Long chatId = 123L;
        User user = User.builder().id(chatId).subscribed(true).build();

        when(userRepository.findById(chatId)).thenReturn(Optional.of(user));

        userService.changeSubscription(chatId);

        assertFalse(user.isSubscribed());
        verify(userRepository).save(user);
    }

    @Test
    public void testChangeSubscription_UserNotFound() {
        Long chatId = 123L;
        when(userRepository.findById(chatId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> userService.changeSubscription(chatId));
    }

    @Test
    public void testSetUserSite_ExistingUser() {
        Long chatId = 123L;
        User existingUser = User.builder().id(chatId).site(UserSite.DONOR_MOS).build();
        UserSite newSite = UserSite.DONOR_MOS_ZAR;

        when(userRepository.findById(chatId)).thenReturn(Optional.of(existingUser));

        userService.setUserSite(chatId, newSite);

        assertEquals(newSite, existingUser.getSite());
        verify(userRepository).save(existingUser);
    }

    @Test
    public void testSetUserSite_NewUser() {
        Long chatId = 123L;
        UserSite site = UserSite.DONOR_MOS_SAB;

        when(userRepository.findById(chatId)).thenReturn(Optional.empty());

        userService.setUserSite(chatId, site);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertEquals(chatId, savedUser.getId());
        assertEquals(site, savedUser.getSite());
        assertFalse(savedUser.isActive());
        assertTrue(savedUser.isSubscribed());
    }

    @Test
    public void testHasCredentials_Valid() {
        Long chatId = 123L;
        User user = User.builder().id(chatId).email("test@example.com").password("pass").build();

        when(userRepository.findById(chatId)).thenReturn(Optional.of(user));

        assertTrue(userService.hasCredentials(chatId));
    }

    @Test
    public void testHasCredentials_MissingEmail() {
        Long chatId = 123L;
        User user = User.builder().id(chatId).password("pass").build();

        when(userRepository.findById(chatId)).thenReturn(Optional.of(user));

        assertFalse(userService.hasCredentials(chatId));
    }

    @Test
    public void testHasCredentials_EmptyPassword() {
        Long chatId = 123L;
        User user = User.builder().id(chatId).email("test@example.com").password("").build();

        when(userRepository.findById(chatId)).thenReturn(Optional.of(user));

        assertFalse(userService.hasCredentials(chatId));
    }

    @Test
    public void testHasCredentials_UserNotFound() {
        Long chatId = 123L;
        when(userRepository.findById(chatId)).thenReturn(Optional.empty());

        assertFalse(userService.hasCredentials(chatId));
    }

    @Test
    public void testDeleteUser_WithSpots() {
        Long chatId = 123L;
        User user = User.builder().id(chatId).email("test@example.com").build();
        List<com.pet_projects.bloodspotbotapi.model.Spot> spots = List
                .of(new com.pet_projects.bloodspotbotapi.model.Spot());

        when(userRepository.findById(chatId)).thenReturn(Optional.of(user));
        when(spotRepository.findAllByUser(user)).thenReturn(spots);

        userService.deleteUser(chatId);

        verify(spotRepository).deleteAll(spots);
        verify(userRepository).delete(user);
    }

    @Test
    public void testDeleteUser_NoSpots() {
        Long chatId = 123L;
        User user = User.builder().id(chatId).email("test@example.com").build();

        when(userRepository.findById(chatId)).thenReturn(Optional.of(user));
        when(spotRepository.findAllByUser(user)).thenReturn(Collections.emptyList());

        userService.deleteUser(chatId);

        verify(spotRepository).deleteAll(Collections.emptyList());
        verify(userRepository).delete(user);
    }

    @Test
    public void testDeleteUser_NotFound() {
        Long chatId = 123L;
        when(userRepository.findById(chatId)).thenReturn(Optional.empty());

        userService.deleteUser(chatId);

        verify(spotRepository, never()).findAllByUser(any());
        verify(userRepository, never()).delete(any());
    }
}

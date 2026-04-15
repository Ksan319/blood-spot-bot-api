package com.pet_projects.bloodspotbotapi.service;

import com.pet_projects.bloodspotbotapi.model.Admin;
import com.pet_projects.bloodspotbotapi.repository.AdminRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AdminServiceTest {

    private static final String ADMIN_PASSWORD = "secure_admin_pwd";

    private AdminService adminService;

    @Mock
    private AdminRepository adminRepository;

    @BeforeEach
    public void setUp() {
        adminService = new AdminService(adminRepository);
        ReflectionTestUtils.setField(adminService, "adminPassword", ADMIN_PASSWORD);
    }

    @Test
    public void testIsAdmin_True() {
        Long chatId = 100L;
        when(adminRepository.existsById(chatId)).thenReturn(true);

        assertTrue(adminService.isAdmin(chatId));
    }

    @Test
    public void testIsAdmin_False() {
        Long chatId = 200L;
        when(adminRepository.existsById(chatId)).thenReturn(false);

        assertFalse(adminService.isAdmin(chatId));
    }

    @Test
    public void testVerifyPassword_Correct() {
        assertTrue(adminService.verifyPassword(ADMIN_PASSWORD));
    }

    @Test
    public void testVerifyPassword_Incorrect() {
        assertFalse(adminService.verifyPassword("wrong_password"));
    }

    @Test
    public void testRegisterAdmin() {
        Long chatId = 100L;

        adminService.registerAdmin(chatId);

        ArgumentCaptor<Admin> captor = ArgumentCaptor.forClass(Admin.class);
        verify(adminRepository).save(captor.capture());

        Admin saved = captor.getValue();
        assertEquals(chatId, saved.getId());
        assertNotNull(saved.getCreatedAt());
    }
}

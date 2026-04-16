package com.pet_projects.bloodspotbotapi.bot.command;

import com.pet_projects.bloodspotbotapi.bot.client.TelegramClientWrapper;
import com.pet_projects.bloodspotbotapi.service.AdminService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.chat.Chat;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AdminAuthCommandTest {

    private AdminAuthCommand adminAuthCommand;

    @Mock
    private AdminService adminService;

    @Mock
    private TelegramClientWrapper telegramClientWrapper;

    @BeforeEach
    public void setUp() {
        adminAuthCommand = new AdminAuthCommand(adminService, telegramClientWrapper);
    }

    @Test
    public void testCommand() {
        assertEquals("/admin-auth", adminAuthCommand.command());
    }

    @Test
    public void testSupports_ExactMatch() {
        assertTrue(adminAuthCommand.supports("/admin-auth"));
    }

    @Test
    public void testSupports_OtherCommand() {
        assertFalse(adminAuthCommand.supports("/start"));
        assertFalse(adminAuthCommand.supports("/admin-stats"));
    }

    @Test
    public void testProcess_CorrectPassword() throws Exception {
        Long chatId = 100L;
        Update update = buildUpdate(chatId, "/admin-auth secure_pwd");

        when(adminService.verifyPassword("secure_pwd")).thenReturn(true);

        adminAuthCommand.process(chatId, update);

        verify(adminService).registerAdmin(chatId);
        verify(telegramClientWrapper).sendMethod(any(SendMessage.class));
    }

    @Test
    public void testProcess_WrongPassword() throws Exception {
        Long chatId = 100L;
        Update update = buildUpdate(chatId, "/admin-auth wrong_pwd");

        when(adminService.verifyPassword("wrong_pwd")).thenReturn(false);

        adminAuthCommand.process(chatId, update);

        verify(adminService, never()).registerAdmin(any());
        ArgumentCaptor<SendMessage> captor = ArgumentCaptor.forClass(SendMessage.class);
        verify(telegramClientWrapper).sendMethod(captor.capture());
        assertEquals("Неизвестная команда", captor.getValue().getText());
    }

    @Test
    public void testProcess_NoPassword() throws Exception {
        Long chatId = 100L;
        Update update = buildUpdate(chatId, "/admin-auth");

        adminAuthCommand.process(chatId, update);

        verify(adminService, never()).verifyPassword(any());
        verify(adminService, never()).registerAdmin(any());
        ArgumentCaptor<SendMessage> captor = ArgumentCaptor.forClass(SendMessage.class);
        verify(telegramClientWrapper).sendMethod(captor.capture());
        assertEquals("Неизвестная команда", captor.getValue().getText());
    }

    @Test
    public void testProcess_EmptyPassword() throws Exception {
        Long chatId = 100L;
        Update update = buildUpdate(chatId, "/admin-auth  ");

        adminAuthCommand.process(chatId, update);

        verify(adminService, never()).verifyPassword(any());
        verify(adminService, never()).registerAdmin(any());
    }

    private Update buildUpdate(Long chatId, String text) {
        Update update = new Update();
        Chat chat = Chat.builder().id(chatId).type("private").build();
        Message message = Message.builder().chat(chat).text(text).build();
        update.setMessage(message);
        return update;
    }
}

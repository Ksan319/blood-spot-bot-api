package com.pet_projects.bloodspotbotapi.bot.command;

import com.pet_projects.bloodspotbotapi.bot.client.TelegramClientWrapper;
import com.pet_projects.bloodspotbotapi.service.AdminService;
import com.pet_projects.bloodspotbotapi.service.SiteErrorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.chat.Chat;
import org.telegram.telegrambots.meta.api.objects.message.Message;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AdminErrorsCommandTest {

    private AdminErrorsCommand adminErrorsCommand;

    @Mock
    private AdminService adminService;

    @Mock
    private TelegramClientWrapper telegramClientWrapper;

    @Mock
    private SiteErrorService siteErrorService;

    @BeforeEach
    public void setUp() {
        adminErrorsCommand = new AdminErrorsCommand(adminService, telegramClientWrapper, siteErrorService);
    }

    @Test
    public void testCommand() {
        assertEquals("/admin-errors", adminErrorsCommand.command());
    }

    @Test
    public void testSupports_ExactMatch() {
        assertTrue(adminErrorsCommand.supports("/admin-errors"));
    }

    @Test
    public void testSupports_OtherCommand() {
        assertFalse(adminErrorsCommand.supports("/admin-stats"));
        assertFalse(adminErrorsCommand.supports("/admin-auth"));
        assertFalse(adminErrorsCommand.supports("/start"));
    }

    @Test
    public void testProcess_AdminGetsErrorsReport() throws Exception {
        Long chatId = 100L;
        Update update = buildUpdate(chatId, "/admin-errors");
        String errorsReport = "Последние ошибки (2):\n\n1. 2026-05-21 10:30 | Поликарпова | SITE_UNAVAILABLE";

        when(adminService.isAdmin(chatId)).thenReturn(true);
        when(siteErrorService.formatLastErrors()).thenReturn(errorsReport);

        adminErrorsCommand.process(chatId, update);

        ArgumentCaptor<SendMessage> captor = ArgumentCaptor.forClass(SendMessage.class);
        verify(telegramClientWrapper).sendMethod(captor.capture());

        assertEquals(errorsReport, captor.getValue().getText());
    }

    @Test
    public void testProcess_NonAdminGetsUnknownCommand() throws Exception {
        Long chatId = 200L;
        Update update = buildUpdate(chatId, "/admin-errors");

        when(adminService.isAdmin(chatId)).thenReturn(false);

        adminErrorsCommand.process(chatId, update);

        ArgumentCaptor<SendMessage> captor = ArgumentCaptor.forClass(SendMessage.class);
        verify(telegramClientWrapper).sendMethod(captor.capture());
        assertEquals("Неизвестная команда", captor.getValue().getText());
        verify(siteErrorService, never()).formatLastErrors();
    }

    @Test
    public void testProcess_AdminGetsEmptyErrorsReport() throws Exception {
        Long chatId = 100L;
        Update update = buildUpdate(chatId, "/admin-errors");

        when(adminService.isAdmin(chatId)).thenReturn(true);
        when(siteErrorService.formatLastErrors()).thenReturn("Нет записанных ошибок.");

        adminErrorsCommand.process(chatId, update);

        ArgumentCaptor<SendMessage> captor = ArgumentCaptor.forClass(SendMessage.class);
        verify(telegramClientWrapper).sendMethod(captor.capture());

        assertEquals("Нет записанных ошибок.", captor.getValue().getText());
    }

    private Update buildUpdate(Long chatId, String text) {
        Update update = new Update();
        Chat chat = Chat.builder().id(chatId).type("private").build();
        Message message = Message.builder().chat(chat).text(text).build();
        update.setMessage(message);
        return update;
    }
}

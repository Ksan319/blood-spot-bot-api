package com.pet_projects.bloodspotbotapi.bot.command;

import com.pet_projects.bloodspotbotapi.bot.client.TelegramClientWrapper;
import com.pet_projects.bloodspotbotapi.repository.UserRepository;
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
public class AdminStatsCommandTest {

    private AdminStatsCommand adminStatsCommand;

    @Mock
    private AdminService adminService;

    @Mock
    private TelegramClientWrapper telegramClientWrapper;

    @Mock
    private UserRepository userRepository;

    @BeforeEach
    public void setUp() {
        adminStatsCommand = new AdminStatsCommand(adminService, telegramClientWrapper, userRepository);
    }

    @Test
    public void testCommand() {
        assertEquals("/admin-stats", adminStatsCommand.command());
    }

    @Test
    public void testSupports_ExactMatch() {
        assertTrue(adminStatsCommand.supports("/admin-stats"));
    }

    @Test
    public void testSupports_OtherCommand() {
        assertFalse(adminStatsCommand.supports("/admin-auth"));
        assertFalse(adminStatsCommand.supports("/start"));
    }

    @Test
    public void testProcess_AdminGetsStats() throws Exception {
        Long chatId = 100L;
        Update update = buildUpdate(chatId, "/admin-stats");

        when(adminService.isAdmin(chatId)).thenReturn(true);
        when(userRepository.count()).thenReturn(50L);
        when(userRepository.countBySubscribed(true)).thenReturn(35L);

        adminStatsCommand.process(chatId, update);

        ArgumentCaptor<SendMessage> captor = ArgumentCaptor.forClass(SendMessage.class);
        verify(telegramClientWrapper).sendMethod(captor.capture());

        String text = captor.getValue().getText();
        assertTrue(text.contains("50"));
        assertTrue(text.contains("35"));
    }

    @Test
    public void testProcess_NonAdminGetsUnknownCommand() throws Exception {
        Long chatId = 200L;
        Update update = buildUpdate(chatId, "/admin-stats");

        when(adminService.isAdmin(chatId)).thenReturn(false);

        adminStatsCommand.process(chatId, update);

        ArgumentCaptor<SendMessage> captor = ArgumentCaptor.forClass(SendMessage.class);
        verify(telegramClientWrapper).sendMethod(captor.capture());
        assertEquals("Неизвестная команда", captor.getValue().getText());
        verify(userRepository, never()).count();
    }

    private Update buildUpdate(Long chatId, String text) {
        Update update = new Update();
        Chat chat = Chat.builder().id(chatId).type("private").build();
        Message message = Message.builder().chat(chat).text(text).build();
        update.setMessage(message);
        return update;
    }
}

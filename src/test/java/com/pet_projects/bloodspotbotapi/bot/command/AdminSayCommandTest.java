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

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AdminSayCommandTest {

    private AdminSayCommand adminSayCommand;

    @Mock
    private AdminService adminService;

    @Mock
    private TelegramClientWrapper telegramClientWrapper;

    @Mock
    private UserRepository userRepository;

    @BeforeEach
    public void setUp() {
        adminSayCommand = new AdminSayCommand(adminService, telegramClientWrapper, userRepository);
    }

    @Test
    public void testCommand() {
        assertEquals("/admin-say", adminSayCommand.command());
    }

    @Test
    public void testSupports_ExactMatch() {
        assertTrue(adminSayCommand.supports("/admin-say"));
    }

    @Test
    public void testSupports_OtherCommand() {
        assertFalse(adminSayCommand.supports("/admin-auth"));
        assertFalse(adminSayCommand.supports("/start"));
    }

    @Test
    public void testProcess_NonAdminGetsUnknownCommand() throws Exception {
        Long chatId = 200L;
        Update update = buildUpdate(chatId, "/admin-say Hello");

        when(adminService.isAdmin(chatId)).thenReturn(false);

        adminSayCommand.process(chatId, update);

        ArgumentCaptor<SendMessage> captor = ArgumentCaptor.forClass(SendMessage.class);
        verify(telegramClientWrapper).sendMethod(captor.capture());
        assertEquals("Неизвестная команда", captor.getValue().getText());
        verify(userRepository, never()).findAllIds();
    }

    @Test
    public void testProcess_EmptyText() throws Exception {
        Long chatId = 100L;
        Update update = buildUpdate(chatId, "/admin-say");

        when(adminService.isAdmin(chatId)).thenReturn(true);

        adminSayCommand.process(chatId, update);

        ArgumentCaptor<SendMessage> captor = ArgumentCaptor.forClass(SendMessage.class);
        verify(telegramClientWrapper).sendMethod(captor.capture());
        assertEquals("Неизвестная команда", captor.getValue().getText());
        verify(userRepository, never()).findAllIds();
    }

    @Test
    public void testProcess_BlankText() throws Exception {
        Long chatId = 100L;
        Update update = buildUpdate(chatId, "/admin-say   ");

        when(adminService.isAdmin(chatId)).thenReturn(true);

        adminSayCommand.process(chatId, update);

        verify(userRepository, never()).findAllIds();
    }

    @Test
    public void testProcess_SuccessfulBroadcast() throws Exception {
        Long adminChatId = 100L;
        Update update = buildUpdate(adminChatId, "/admin-say Внимание!");

        when(adminService.isAdmin(adminChatId)).thenReturn(true);
        when(userRepository.findAllIds()).thenReturn(List.of(1L, 2L, 3L));

        adminSayCommand.process(adminChatId, update);

        // 3 messages to users + 1 summary to admin = 4 total
        verify(telegramClientWrapper, times(4)).sendMethod(any(SendMessage.class));

        ArgumentCaptor<SendMessage> captor = ArgumentCaptor.forClass(SendMessage.class);
        verify(telegramClientWrapper, times(4)).sendMethod(captor.capture());

        List<SendMessage> allMessages = captor.getAllValues();
        SendMessage summary = allMessages.get(3);
        assertTrue(summary.getText().contains("3 из 3"));
    }

    @Test
    public void testProcess_PartialBroadcastFailure() throws Exception {
        Long adminChatId = 100L;
        Update update = buildUpdate(adminChatId, "/admin-say Тест");

        when(adminService.isAdmin(adminChatId)).thenReturn(true);
        when(userRepository.findAllIds()).thenReturn(List.of(1L, 2L, 3L));

        // Use a counter to throw on the second call only
        final int[] callCount = {0};
        doAnswer(invocation -> {
            callCount[0]++;
            if (callCount[0] == 2) {
                throw new RuntimeException("Blocked");
            }
            return null;
        }).when(telegramClientWrapper).sendMethod(any(SendMessage.class));

        adminSayCommand.process(adminChatId, update);

        ArgumentCaptor<SendMessage> captor = ArgumentCaptor.forClass(SendMessage.class);
        verify(telegramClientWrapper, atLeast(3)).sendMethod(captor.capture());

        SendMessage summary = captor.getAllValues().get(captor.getAllValues().size() - 1);
        assertTrue(summary.getText().contains("2 из 3"), "Expected summary to contain '2 из 3' but was: " + summary.getText());
    }

    private Update buildUpdate(Long chatId, String text) {
        Update update = new Update();
        Chat chat = Chat.builder().id(chatId).type("private").build();
        Message message = Message.builder().chat(chat).text(text).build();
        update.setMessage(message);
        return update;
    }
}

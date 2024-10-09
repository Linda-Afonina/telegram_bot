package pro.sky.telegrambot.service;

import com.pengrad.telegrambot.TelegramBot;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import pro.sky.telegrambot.model.NotificationTask;
import pro.sky.telegrambot.repository.NotificationTaskRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NotificationTaskServiceTest {

    @Mock
    private NotificationTaskRepository notificationTaskRepository;

    @Mock
    private TelegramBot telegramBot;

    @InjectMocks
    private NotificationTaskService notificationTaskService;

    private final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    @Test
    public void shouldParseAndSaveInValidFormat() {
        long expectedChatId = 35346L;
        String expectedMessage = "01.11.2024 20:00 Встреча с друзьями";
        NotificationTask expectedNotificationTask = new NotificationTask();
        expectedNotificationTask.setId(expectedChatId);
        expectedNotificationTask.setMessage(expectedMessage);
        expectedNotificationTask.setNotificationDateTime(LocalDateTime.parse("01.11.2024 20:00", DATE_TIME_FORMATTER));

        Mockito.when(notificationTaskRepository.save(any(NotificationTask.class))).thenReturn(expectedNotificationTask);

        notificationTaskService.parseAndSaveNotificationTask(expectedChatId, expectedMessage);
        verify(notificationTaskRepository, times(1)).save(any(NotificationTask.class));
    }

    @Test
    public void shouldParseAndSaveInInvalidFormat() {
        long expectedChatId = 35346L;
        String expectedMessage = "Неверный формат. Используйте формат: 'dd.MM.yyyy HH:mm Текст напоминания'.";
        notificationTaskService.parseAndSaveNotificationTask(expectedChatId, expectedMessage);
        verify(notificationTaskRepository, never()).save(any(NotificationTask.class));
    }
}

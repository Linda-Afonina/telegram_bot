package pro.sky.telegrambot.service;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.model.NotificationTask;
import pro.sky.telegrambot.repository.NotificationTaskRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class NotificationTaskService {
    private final NotificationTaskRepository notificationTaskRepository;
    private final TelegramBot telegramBot;
    private Logger logger = LoggerFactory.getLogger(NotificationTaskService.class);
    private final Pattern NOTIFICATION_DATE_TIME_PATTERN = Pattern
            .compile("(\\d{2}\\.\\d{2}\\.\\d{4}\\s\\d{2}:\\d{2})(\\s+)(.+)");
    private final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    public NotificationTaskService(NotificationTaskRepository notificationTaskRepository, TelegramBot telegramBot) {
        this.notificationTaskRepository = notificationTaskRepository;
        this.telegramBot = telegramBot;
    }

    public void parseAndSaveNotificationTask(long chatID, String message) {
        Matcher matcher = NOTIFICATION_DATE_TIME_PATTERN.matcher(message);
        if (matcher.matches()) {
            String dateTimeString = matcher.group(1);
            String reminderText = matcher.group(3);

            LocalDateTime dateTime = LocalDateTime.parse(dateTimeString, DATE_TIME_FORMATTER);

            NotificationTask notificationTask = new NotificationTask();
            notificationTask.setChatId(chatID);
            notificationTask.setMessage(reminderText);
            notificationTask.setNotificationDateTime(dateTime);

            notificationTaskRepository.save(notificationTask);

            sendMessage(chatID, "Напоминание успешно сохранено!");
        } else {
            sendMessage(chatID,
                    "Неверный формат. Используйте формат: 'dd.MM.yyyy HH:mm Текст напоминания'.");
        }
    }

    public void sendMessage(long chatID, String message) {
        SendMessage sendMessage = new SendMessage(chatID, message);
        try {
            telegramBot.execute(sendMessage);
        } catch (RuntimeException e) {
            logger.error("Error sending message: {}", e.getMessage());
        }
    }

    @Scheduled(cron = "0 0/1 * * * *")
    public void sendNotificationTask() {
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        List<NotificationTask> notificationTaskList = notificationTaskRepository.findByNotificationDateTime(now);

        for (NotificationTask notificationTask : notificationTaskList) {
            String message = "Напоминание: " + notificationTask.getMessage();
            long chatId = notificationTask.getChatId();
            sendMessage(chatId, message);
        }
    }
}

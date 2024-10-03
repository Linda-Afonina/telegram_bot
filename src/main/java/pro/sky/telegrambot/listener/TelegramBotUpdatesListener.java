package pro.sky.telegrambot.listener;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pro.sky.telegrambot.model.NotificationTask;
import pro.sky.telegrambot.repository.NotificationTaskRepository;

import javax.annotation.PostConstruct;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class TelegramBotUpdatesListener implements UpdatesListener {

    private Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);
    private final Pattern NOTIFICATION_DATE_TIME_PATTERN = Pattern
            .compile("(\\d{2}\\.\\d{2}\\.\\d{4}\\s\\d{2}:\\d{2})(\\s+)(.+)");

    private final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    @Autowired
    private TelegramBot telegramBot;

    @Autowired
    private NotificationTaskRepository notificationTaskRepository;

    @PostConstruct
    public void init() {
        telegramBot.setUpdatesListener(this);
    }

    @Override
    public int process(List<Update> updates) {
        try {
            updates.forEach(update -> {
                logger.info("Processing update: {}", update);
                // Process your updates here
                String start = "/start";
                String message = update.message().text();
                long chatID = update.message().chat().id();
                if (start.equals(message)) {
                    sendMessage(chatID, "Добро пожаловать в бота!");
                } else {
                    parseAndSaveNotificationTask(chatID, message);
                }
            });
        } catch (RuntimeException e) {
            logger.error(e.getMessage());
        }
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
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

    private void sendMessage(long chatID, String message) {
        SendMessage sendMessage = new SendMessage(chatID, message);
        try {
            telegramBot.execute(sendMessage);
        } catch (RuntimeException e) {
            logger.error("Error sending message: {}", e.getMessage());
        }
    }


}

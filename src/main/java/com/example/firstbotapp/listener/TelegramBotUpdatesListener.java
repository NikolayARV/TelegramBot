package com.example.firstbotapp.listener;

import com.example.firstbotapp.entity.NotificationTask;
import com.example.firstbotapp.service.NotificationTaskService;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.el.parser.ELParser.parse;

@Component
public class TelegramBotUpdatesListener implements UpdatesListener {
    private final TelegramBot telegramBot;
    private final NotificationTaskService notificationTaskService;
    private final Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);
    private final Pattern pattern = Pattern.compile(
            "(\\d{1,2}\\.\\d{1,2}\\.\\d{4} \\d{1,2}:\\d{2}) ([А-я\\d\\s.,!?:]+)"
    );

    public TelegramBotUpdatesListener(TelegramBot telegramBot, NotificationTaskService notificationTaskService) {
        this.telegramBot = telegramBot;
        this.notificationTaskService = notificationTaskService;
    }


    @PostConstruct
    public void init() {
        telegramBot.setUpdatesListener(this);
    }

    @Override
    public int process(List<Update> updates) {
        try {
            updates.stream()
                            .filter(update -> update.message() !=null)
                    .forEach(update -> {
                logger.info("Handles update: {}", update);
                Message message = update.message();
                Long chatId = message.chat().id();
                String text = message.text();

                if ("/start".equals(text)) {
                    sendMessage(chatId, "Привет! Я помогу тебе запланировать задачу. Отправь её в формате: 12.03.2023 21:00 Сделать домашку ");
                } else if (text != null) {
                    Matcher matcher = pattern.matcher(text);
                    if (matcher.find()) {
                        LocalDateTime dateTime = LocalDateTime.parse(matcher.group(1));
                        if (Objects.isNull(dateTime)) {
                            sendMessage(chatId, "Некорректный формат даты и/или времени");
                        } else {
                            String txt = matcher.group(2);
                            NotificationTask notificationTask = new NotificationTask();
                            notificationTask.setChatId(chatId);
                            notificationTask.setMessage(txt);
                            notificationTask.setNotificationDateTime(dateTime);
                            notificationTaskService.save(notificationTask);
                            sendMessage(chatId, "Задача успешно запланирована!");
                    }
                }

            }
        });
    } catch(
    Exception e)

    {
        logger.error(e.getMessage(), e);
    }
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
}

    private void sendMessage(Long chatId, String message) {
        SendMessage sendMessage = new SendMessage(chatId, message);
        SendResponse sendResponse = telegramBot.execute(sendMessage);
        if (!sendResponse.isOk()) {
            logger.error("Error during sending message: {}", sendResponse.description());
        }
    }
}

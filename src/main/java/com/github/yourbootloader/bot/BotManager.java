package com.github.yourbootloader.bot;

import com.github.yourbootloader.bot.event.FinishDownloadEvent;
import com.github.yourbootloader.bot.event.ProgressIndicatorEvent;
import com.github.yourbootloader.bot.event.StartDownloadEvent;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.util.unit.DataSize;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendAudio;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BotManager {

    private final Bot bot;
    private static final Map<Chat, Map<String, Object>> cache = new HashMap<>();

    @EventListener
    public void onStartDownloadProcess(StartDownloadEvent event) {
        log.info("Скачивание начинается...");
        Long chatId = event.getChat().getId();
        SendMessage message = new SendMessage(String.valueOf(chatId), "Скачивание начинается...");

        try {
            Message msg = bot.execute(message);
            cache.put(event.getChat(), new HashMap<String, Object>() {{
                put("messageId", msg.getMessageId());
                put("chatId", chatId);
            }});
        } catch (TelegramApiException e) {
            log.error("Возникла непредвиденная ошибка", e);
            sendNotification(chatId, e.getMessage());
        }
    }

    @EventListener
    public void onProgressIndicatorEvent(ProgressIndicatorEvent event) {
        Map<String, Object> data = cache.get(event.getChat());
        Long chatId = ((Long) data.get("chatId"));
        Integer messageId = (Integer) data.get("messageId");
        DataSize dataSize = DataSize.ofBytes(event.getFileSize());
        long downloadedContent = Long.rotateLeft(dataSize.toKilobytes(), 3);
        long contentSize = Long.rotateLeft(event.getContentSize().toKilobytes(), 3);
        int percent = (int) ((downloadedContent * 100.0) / contentSize);
        log.info("Скачано " + downloadedContent + " Kb из " + contentSize + " Kb [" + percent + "%]");

        EditMessageText message = new EditMessageText("Скачано " + downloadedContent + " Kb из " + contentSize + " Kb [" + percent + "%]");
        message.setChatId(chatId.toString());
        message.setMessageId(messageId);
        message.setParseMode(ParseMode.HTML);
        message.disableWebPagePreview();

        try {
            bot.execute(message);
        } catch (TelegramApiException e) {
            log.error("Возникла непредвиденная ошибка", e);
            sendNotification(chatId, e.getMessage());
        }
    }

    @EventListener
    public void onFinishDownloadProcess(FinishDownloadEvent event) {
        log.info("Содержимое скачано!");
        Map<String, Object> data = cache.get(event.getChat());
        Long chatId = ((Long) data.get("chatId"));
        File downloadFile = event.getDownloadFile();

        SendAudio SendAudioRequest = new SendAudio();
        SendAudioRequest.setChatId(String.valueOf(chatId));
        SendAudioRequest.setAudio(new InputFile(downloadFile));
        SendAudioRequest.setCaption(downloadFile.getName());
        try {
            bot.execute(SendAudioRequest);
        } catch (TelegramApiException e) {
            log.error("Возникла непредвиденная ошибка", e);
            sendNotification(chatId, "Не удалось скачать аудио. Возникла ошибка: " + e.getMessage());
        }
    }

    @SneakyThrows
    private void sendNotification(Long chatId, String text) {
        SendMessage message = new SendMessage(chatId.toString(), text);
        bot.execute(message);
    }
}

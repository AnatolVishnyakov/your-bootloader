package com.github.yourbootloader.bot;

import com.github.yourbootloader.bot.event.FinishDownloadEvent;
import com.github.yourbootloader.bot.event.ProgressIndicatorEvent;
import com.github.yourbootloader.bot.event.StartDownloadEvent;
import com.github.yourbootloader.yt.exception.MethodNotImplementedException;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BotManager {

    private static final String UNEXPECTED_ERROR = "An unexpected error occurred";
    private static final Map<Chat, Map<String, Object>> cache = new HashMap<>();
    private final ThreadLocal<Map<Long, Integer>> threadLocal = ThreadLocal.withInitial(HashMap::new);
    private final Bot bot;

    @EventListener
    public void onStartDownloadProcess(StartDownloadEvent event) {
        log.info("Downloading start...");
        Optional<Chat> chat = Optional.ofNullable(event.getChat());
        if (!chat.isPresent()) {
            log.warn("Chat doesn't found!");
            return;
        }

        Long chatId = chat.get().getId();
        threadLocal.get().put(chatId, 0);
        SendMessage message = new SendMessage(String.valueOf(chatId), "Скачивание начинается..."); // TODO борьба с лимитом на обновление

        try {
            Message msg = bot.execute(message);
            cache.put(event.getChat(), new HashMap<String, Object>() {{
                put("messageId", msg.getMessageId());
                put("chatId", chatId);
            }});
        } catch (TelegramApiException e) {
            log.error(UNEXPECTED_ERROR, e);
            sendNotification(chatId, e.getMessage());
        }
    }

    @EventListener
    public void onProgressIndicatorEvent(ProgressIndicatorEvent event) {
        Optional<Chat> chat = Optional.ofNullable(event.getChat());
        if (!chat.isPresent()) {
            log.warn("Chat doesn't found!");
            return;
        }

        Map<String, Object> data = cache.get(chat.get().getId());
        Long chatId = ((Long) data.get("chatId"));
        threadLocal.get().putIfAbsent(chatId, 0);
        Integer messageId = (Integer) data.get("messageId");
        DataSize dataSize = DataSize.ofBytes(event.getFileSize());
        long downloadedContent = dataSize.toKilobytes();
        long contentSize = event.getContentSize().toKilobytes();
        int percent = (int) ((downloadedContent * 100.0) / contentSize);
        if (percent != threadLocal.get().get(chatId)) {
            threadLocal.get().put(chatId, percent);
            log.info("Download " + downloadedContent + " Kb of " + contentSize + " Kb [" + percent + "%]");

            EditMessageText message = new EditMessageText("Скачано " + downloadedContent + " Kb из " + contentSize + " Kb [" + percent + "%]");
            message.setChatId(chatId.toString());
            message.setMessageId(messageId);
            message.setParseMode(ParseMode.HTML);
            message.disableWebPagePreview();

            try {
                bot.execute(message);
            } catch (TelegramApiException e) {
                log.error(UNEXPECTED_ERROR, e);
                sendNotification(chatId, e.getMessage());
            }
        }
    }

    @EventListener
    public void onFinishDownloadProcess(FinishDownloadEvent event) {
        log.info("Complete the download process!");
        Optional<Chat> chat = Optional.ofNullable(event.getChat());
        if (!chat.isPresent()) {
            log.warn("Chat doesn't found!");
            return;
        }

        Map<String, Object> data = cache.get(chat.get().getId());
        Long chatId = ((Long) data.get("chatId"));
        File downloadFile = event.getDownloadFile();

        SendAudio SendAudioRequest = new SendAudio();
        SendAudioRequest.setChatId(String.valueOf(chatId));
        SendAudioRequest.setAudio(new InputFile(downloadFile));
        SendAudioRequest.setCaption(downloadFile.getName());
        try {
            bot.execute(SendAudioRequest);
        } catch (TelegramApiException e) {
            log.error(UNEXPECTED_ERROR, e);
            sendNotification(chatId, "Не удалось скачать аудио. Возникла ошибка: " + e.getMessage());
        }
    }

    @SneakyThrows
    private void sendNotification(Long chatId, String text) {
        SendMessage message = new SendMessage(chatId.toString(), text);
        bot.execute(message);
    }
}

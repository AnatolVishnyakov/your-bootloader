package com.github.yourbootloader.bot;

import com.github.yourbootloader.bot.event.FinishDownloadEvent;
import com.github.yourbootloader.bot.event.ProgressIndicatorEvent;
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
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Profile("prod")
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BotManager {

    private static final String UNEXPECTED_ERROR = "An unexpected error occurred";
    private static final Map<UUID, Integer> messageCache = new HashMap<>();
    private final ThreadLocal<Map<Long, Long>> threadLocal = ThreadLocal.withInitial(HashMap::new);
    private final Bot bot;

    @EventListener
    public void onProgressIndicatorEvent(ProgressIndicatorEvent event) {
        Long chatId = event.getChat().getId();
        threadLocal.get().putIfAbsent(chatId, 0L);

        int messageId = messageCache.computeIfAbsent(event.getDownloadId(), uuid -> {
            SendMessage message = new SendMessage(String.valueOf(chatId), "Скачивание начинается..."); // TODO борьба с лимитом на обновление

            try {
                Message msg = bot.execute(message);
                return msg.getMessageId();
            } catch (TelegramApiException e) {
                log.error(UNEXPECTED_ERROR, e);
                sendNotification(chatId, e.getMessage());
                throw new RuntimeException(e);
            }
        });

        DataSize dataSize = DataSize.ofBytes(event.getFileSize());
        long downloadedContent = dataSize.toKilobytes();
        long contentSize = event.getContentSize().toKilobytes();
        int percent = (int) ((downloadedContent * 100.0) / contentSize);
        if (downloadedContent != threadLocal.get().get(chatId)) {
            threadLocal.get().put(chatId, downloadedContent);
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
        Long chatId = event.getChat().getId();
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

package com.github.yourbootloader.yt.download.listener;

import com.github.yourbootloader.bot.Bot;
import io.netty.handler.codec.http.HttpHeaders;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.asynchttpclient.handler.TransferListener;
import org.springframework.util.unit.DataSize;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendAudio;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

import java.io.File;

@Slf4j
public class TelegramProgressListener implements TransferListener {

    private static final String UNEXPECTED_ERROR = "An unexpected error occurred!";

    private final Bot bot;
    private final Chat chat;
    private final long contentLength;
    private int messageId;
    private long downloadedBytes;
    private long prevTimeMs = System.currentTimeMillis();

    public TelegramProgressListener(Bot bot, Chat chat, long contentLength) {
        this.bot = bot;
        this.chat = chat;
        this.contentLength = contentLength;
    }

    @Override
    public void onRequestHeadersSent(HttpHeaders headers) {
        log.info("call onRequestHeadersSent(...)");
    }

    @Override
    public void onResponseHeadersReceived(HttpHeaders headers) {
        SendMessage message = new SendMessage(String.valueOf(chat.getId()), "Скачивание начинается...");

        try {
            Message msg = bot.execute(message);
            this.messageId = msg.getMessageId();
        } catch (TelegramApiException e) {
            log.error(UNEXPECTED_ERROR, e);
            sendNotification(chat.getId(), e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onBytesReceived(byte[] bytes) {
        long currentTimeMs = System.currentTimeMillis();
        downloadedBytes += bytes.length;
        long delayBetweenReceivingBytes = currentTimeMs - prevTimeMs;

        int percent = (int) ((downloadedBytes * 100.0) / contentLength);
        String msgText = String.format(
                "Скачано %d Kb из %d Kb [%d%%]\nChunk size: %d bytes\nDelay: %d (ms)",
                DataSize.ofBytes(downloadedBytes).toKilobytes(),
                DataSize.ofBytes(contentLength).toKilobytes(),
                percent,
                bytes.length,
                delayBetweenReceivingBytes
        );

        if (Math.abs(currentTimeMs - prevTimeMs) > 500) {
            EditMessageText message = new EditMessageText(msgText);
            message.setChatId(chat.getId().toString());
            message.setMessageId(messageId);
            message.setParseMode(ParseMode.HTML);
            message.disableWebPagePreview();

            try {
                bot.execute(message);
            } catch (Throwable e) {
                log.error(UNEXPECTED_ERROR, e);
                if (e.getMessage().contains("message to edit not found")) {
                    SendMessage msg = new SendMessage(String.valueOf(chat.getId()), msgText);
                    try {
                        messageId = bot.execute(msg).getMessageId();
                    } catch (TelegramApiException ignored) {
                    }
                } else {
                    sendNotification(chat.getId(), e.getMessage());
                }
            }
        }
        prevTimeMs = currentTimeMs;
    }

    @Override
    public void onBytesSent(long amount, long current, long total) {
        log.info("call onBytesSent()");
    }

    @Override
    public void onRequestResponseCompleted() {
        log.info("call onRequestResponseCompleted()");
        onRemoveMessage(chat.getId(), messageId);
    }

    public void onRequestResponseCompleted(File downloadedFile) {
        log.info("Complete the download process!");
        if (downloadedFile.length() <= 0) {
            log.warn("File is empty!");
            return;
        }

        SendAudio SendAudioRequest = new SendAudio();
        SendAudioRequest.setChatId(String.valueOf(chat.getId()));
        SendAudioRequest.setAudio(new InputFile(downloadedFile));
        SendAudioRequest.setCaption(downloadedFile.getName());
        try {
            bot.execute(SendAudioRequest);
        } catch (TelegramApiException e) {
            log.error(UNEXPECTED_ERROR, e);
            sendNotification(chat.getId(), "Не удалось скачать аудио. Возникла ошибка: " + e.getMessage());
        }
        onRemoveMessage(chat.getId(), messageId);
    }

    @Override
    public void onThrowable(Throwable t) {
        log.info("call onThrowable()");
        onRemoveMessage(chat.getId(), messageId);
        sendNotification(chat.getId(), "Error: " + t.getMessage());
    }

    @SneakyThrows
    private void sendNotification(Long chatId, String text) {
        SendMessage message = new SendMessage(chatId.toString(), text);
        bot.execute(message);
    }

    @SneakyThrows
    public void onRemoveMessage(@NonNull Long chatId, int messageId) {
        DeleteMessage message = new DeleteMessage(chatId.toString(), messageId);
        try {
            bot.execute(message);
        } catch (TelegramApiRequestException exc) {
            log.warn(exc.getMessage());
        }
    }
}

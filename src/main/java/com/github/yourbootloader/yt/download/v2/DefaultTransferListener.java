package com.github.yourbootloader.yt.download.v2;

import com.github.yourbootloader.bot.Bot;
import io.netty.handler.codec.http.HttpHeaders;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.asynchttpclient.handler.TransferListener;
import org.springframework.util.unit.DataSize;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Slf4j
@RequiredArgsConstructor
public class DefaultTransferListener implements TransferListener {

    private static final String UNEXPECTED_ERROR = "An unexpected error occurred!";

    private final Bot bot;
    private final Chat chat;
    private final long contentLength;
    private int messageId;
    private long downloadedBytes;
    private long prevTimeMs;

    @Override
    public void onRequestHeadersSent(HttpHeaders headers) {

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

        int percent = (int) ((downloadedBytes * 100.0) / contentLength);
        String msgText = String.format(
                "Скачано %d Kb из %d Kb [%d%%]\nChunk size: %d bytes\nDelay: %d (ms)",
                DataSize.ofBytes(downloadedBytes).toKilobytes(),
                DataSize.ofBytes(contentLength).toKilobytes(),
                percent,
                bytes.length,
                currentTimeMs - prevTimeMs
        );
        log.info(msgText);

        EditMessageText message = new EditMessageText(msgText);
        message.setChatId(chat.getId().toString());
        message.setMessageId(messageId);
        message.setParseMode(ParseMode.HTML);
        message.disableWebPagePreview();

        try {
            bot.execute(message);
        } catch (TelegramApiException e) {
            log.error(UNEXPECTED_ERROR, e);
            sendNotification(chat.getId(), e.getMessage());
        }
        prevTimeMs = currentTimeMs;
    }

    @Override
    public void onBytesSent(long amount, long current, long total) {

    }

    @Override
    public void onRequestResponseCompleted() {

    }

    @Override
    public void onThrowable(Throwable t) {

    }

    @SneakyThrows
    private void sendNotification(Long chatId, String text) {
        SendMessage message = new SendMessage(chatId.toString(), text);
        bot.execute(message);
    }
}

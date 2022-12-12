package com.github.yourbootloader.yt.download.v2;

import com.github.yourbootloader.bot.event.FailureDownloadEvent;
import com.github.yourbootloader.bot.event.FinishDownloadEvent;
import com.github.yourbootloader.bot.event.ProgressIndicatorEvent;
import com.github.yourbootloader.bot.event.StartDownloadEvent;
import io.netty.handler.codec.http.HttpHeaders;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.asynchttpclient.handler.TransferListener;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.util.unit.DataSize;
import org.telegram.telegrambots.meta.api.objects.Chat;

import java.io.File;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
public class ProgressListener implements TransferListener {

    private final ApplicationEventPublisher publisher;

    private final Chat chat;

    private final DataSize contentSize;

    private final File originalFile;

    private final UUID downloadId = UUID.randomUUID();

    private long lastTimeMsReceivedByte;

    @Override
    public void onRequestHeadersSent(HttpHeaders headers) {
        log.info("Headers sent: {}", headers);
        publisher.publishEvent(new StartDownloadEvent(chat));
    }

    @Override
    public void onResponseHeadersReceived(HttpHeaders headers) {
        log.info("Headers received: {}", headers);
    }

    @Override
    public void onBytesReceived(byte[] bytes) {
        long currentTimeMillis = System.currentTimeMillis();
        log.info("Bytes received: {} (delay: {} ms)", bytes.length, currentTimeMillis - lastTimeMsReceivedByte);

        publisher.publishEvent(
                new ProgressIndicatorEvent(
                        chat,
                        contentSize,
                        originalFile.length(),
                        bytes.length,
                        downloadId,
                        currentTimeMillis - lastTimeMsReceivedByte
                )
        );
        lastTimeMsReceivedByte = currentTimeMillis;
    }

    @Override
    public void onBytesSent(long amount, long current, long total) {
        log.info("Bytes sent, amount: {}, current: {}, total: {}", amount, current, total);
    }

    @Override
    public void onRequestResponseCompleted() {
        log.info("Request or response completed.");
        publisher.publishEvent(new FinishDownloadEvent(chat, downloadId, originalFile));
    }

    @Override
    public void onThrowable(Throwable t) {
        log.error("Was error: {}", t.getMessage());
        publisher.publishEvent(new FailureDownloadEvent(chat, t));
    }
}

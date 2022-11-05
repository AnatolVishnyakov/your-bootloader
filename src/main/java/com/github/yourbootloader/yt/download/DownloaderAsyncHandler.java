package com.github.yourbootloader.yt.download;

import com.github.yourbootloader.bot.event.FailureDownloadEvent;
import com.github.yourbootloader.bot.event.FinishDownloadEvent;
import com.github.yourbootloader.bot.event.ProgressIndicatorEvent;
import lombok.extern.slf4j.Slf4j;
import org.asynchttpclient.HttpResponseBodyPart;
import org.asynchttpclient.Response;
import org.asynchttpclient.handler.resumable.ResumableAsyncHandler;
import org.asynchttpclient.handler.resumable.ResumableRandomAccessFileListener;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.retry.annotation.Retryable;
import org.springframework.util.unit.DataSize;
import org.telegram.telegrambots.meta.api.objects.Chat;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Асинхронный обработчик, позволяет продолжить
 * скачивание файла, если по какой-то причине
 * загрузка была прервана.
 */
@Slf4j
@Retryable
class DownloaderAsyncHandler extends ResumableAsyncHandler {

    private final File originalFile;
    private final Chat chat;
    private final UUID downloadId;
    private ApplicationEventPublisher publisher;
    private DataSize contentSize;

    // TODO отладка скорости скачивания
    AtomicLong timeout = new AtomicLong();
    AtomicLong counter = new AtomicLong();
    AtomicReference<Long> time = new AtomicReference<>(System.currentTimeMillis());
    LocalDateTime start = LocalDateTime.now();

    public DownloaderAsyncHandler(Chat chat, File originalFile) throws IOException {
        this.chat = chat;
        this.originalFile = originalFile;
        this.downloadId = UUID.randomUUID();

        RandomAccessFile randomAccessFile = new RandomAccessFile(originalFile, "rw");
        this.setResumableListener(new ResumableRandomAccessFileListener(randomAccessFile) {
            @Override
            public void onBytesReceived(ByteBuffer buffer) throws IOException {
                randomAccessFile.seek(randomAccessFile.length());
                randomAccessFile.write(buffer.array());
            }
        });
    }

    public void setApplicationEventPublisher(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    @Override
    public State onBodyPartReceived(HttpResponseBodyPart bodyPart) throws Exception {
        // TODO отладка скорости скачивания
        log.info("timeout: {}", System.currentTimeMillis() - timeout.get());
        timeout.set(System.currentTimeMillis());

        State state = super.onBodyPartReceived(bodyPart);
        publisher.publishEvent(new ProgressIndicatorEvent(chat, contentSize, originalFile.length(), bodyPart.getBodyPartBytes().length, downloadId));
        return state;
    }

    @Override
    public Response onCompleted() throws Exception {
        publisher.publishEvent(new FinishDownloadEvent(chat, downloadId, originalFile));
        return super.onCompleted();
    }

    @Override
    public void onThrowable(Throwable t) {
        super.onThrowable(t);
        publisher.publishEvent(new FailureDownloadEvent(chat, t));
    }

    public void setContentSize(DataSize contentSize) {
        this.contentSize = contentSize;
    }
}
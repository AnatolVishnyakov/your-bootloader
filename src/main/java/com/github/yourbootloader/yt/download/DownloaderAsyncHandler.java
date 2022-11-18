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
import org.springframework.util.unit.DataSize;
import org.telegram.telegrambots.meta.api.objects.Chat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.util.UUID;

/**
 * Асинхронный обработчик, позволяет продолжить
 * скачивание файла, если по какой-то причине
 * загрузка была прервана.
 */
@Slf4j
class DownloaderAsyncHandler extends ResumableAsyncHandler {

    private final File originalFile;
    private final Chat chat;
    private final UUID downloadId;
    private ApplicationEventPublisher publisher;
    private DataSize contentSize;
    private long prevReceiveTime = System.currentTimeMillis();

    public DownloaderAsyncHandler(Chat chat, File originalFile) throws FileNotFoundException {
        this.chat = chat;
        this.originalFile = originalFile;
        this.downloadId = UUID.randomUUID();

        ResumableRandomAccessFileListener listener = new ResumableRandomAccessFileListener(
                new RandomAccessFile(originalFile, "rw"));
        this.setResumableListener(listener);
    }

    public void setApplicationEventPublisher(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    @Override
    public State onBodyPartReceived(HttpResponseBodyPart bodyPart) throws Exception {
        State state = super.onBodyPartReceived(bodyPart);
        long currentCallTime = System.currentTimeMillis();
        publisher.publishEvent(new ProgressIndicatorEvent(chat, contentSize, originalFile.length(),
                bodyPart.getBodyPartBytes().length, downloadId, currentCallTime - prevReceiveTime));
        prevReceiveTime = currentCallTime;
        return state;
    }

    @Override
    public Response onCompleted() throws Exception {
        log.info("onCompleted and send file in telegram...");
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
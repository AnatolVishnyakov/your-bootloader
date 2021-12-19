package com.github.yourbootloader.yt.download;

import com.github.yourbootloader.bot.event.ProgressIndicatorEvent;
import lombok.extern.slf4j.Slf4j;
import org.asynchttpclient.HttpResponseBodyPart;
import org.asynchttpclient.handler.resumable.ResumableAsyncHandler;
import org.asynchttpclient.handler.resumable.ResumableRandomAccessFileListener;
import org.springframework.context.ApplicationEventPublisher;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;

/**
 * Асинхронный обработчик, позволяет продолжить
 * скачивание файла, если по какой-то причине
 * загрузка была прервана.
 */
@Slf4j
class DownloaderAsyncHandler extends ResumableAsyncHandler {

    private final File originalFile;
    private ApplicationEventPublisher publisher;

    public DownloaderAsyncHandler(File originalFile) throws IOException {
        this.originalFile = originalFile;

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
        State state = super.onBodyPartReceived(bodyPart);
        publisher.publishEvent(new ProgressIndicatorEvent(originalFile.length(), bodyPart.getBodyPartBytes().length));
        return state;
    }
}
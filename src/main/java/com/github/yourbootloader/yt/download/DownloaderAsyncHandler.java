package com.github.yourbootloader.yt.download;

import com.github.yourbootloader.bot.event.ProgressIndicatorEvent;
import lombok.extern.slf4j.Slf4j;
import org.asynchttpclient.HttpResponseBodyPart;
import org.asynchttpclient.handler.resumable.ResumableAsyncHandler;
import org.asynchttpclient.handler.resumable.ResumableRandomAccessFileListener;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.util.unit.DataSize;
import org.telegram.telegrambots.meta.api.objects.Chat;

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
    private final Chat chat;
    private ApplicationEventPublisher publisher;
    private DataSize contentSize;

    public DownloaderAsyncHandler(Chat chat, File originalFile) throws IOException {
        this.chat = chat;
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
        publisher.publishEvent(new ProgressIndicatorEvent(chat, contentSize, originalFile.length(), bodyPart.getBodyPartBytes().length));
        return state;
    }

    public void setContentSize(DataSize contentSize) {
        this.contentSize = contentSize;
    }
}
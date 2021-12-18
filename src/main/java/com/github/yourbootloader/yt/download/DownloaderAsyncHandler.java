package com.github.yourbootloader.yt.download;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.asynchttpclient.HttpResponseBodyPart;
import org.asynchttpclient.handler.resumable.ResumableAsyncHandler;
import org.asynchttpclient.handler.resumable.ResumableRandomAccessFileListener;
import org.springframework.util.unit.DataSize;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.nio.file.Files;

/**
 * Возобновляемый обработчик, позволяет продолжить
 * скачивание файла, если по какой-то причине
 * загрузка была прервана.
 */
@Slf4j
class DownloaderAsyncHandler extends ResumableAsyncHandler {

    private final File originalFile;

    public DownloaderAsyncHandler(File originalFile) throws FileNotFoundException {
        this.originalFile = originalFile;
        setResumableListener(new ResumableRandomAccessFileListener(new RandomAccessFile(originalFile, "rw")));
    }

    @Override
    public State onBodyPartReceived(HttpResponseBodyPart bodyPart) throws Exception {
        State state = super.onBodyPartReceived(bodyPart);
        loggerProgressDownload(bodyPart.getBodyPartBytes().length);
        return state;
    }

    @SneakyThrows
    private void loggerProgressDownload(int receivedLength) {
        long fileSize = Files.size(originalFile.toPath());

        if (fileSize < 1_024) {
            log.info("{} B ({}) block_size: {}", DataSize.ofBytes(fileSize), DataSize.ofBytes(fileSize), receivedLength);
        } else if (fileSize < 1_048_576) {
            log.info("{} Kb ({}) block_size: {}", DataSize.ofBytes(fileSize).toKilobytes(), DataSize.ofBytes(fileSize), receivedLength);
        } else {
            log.info("{} Mb ({}) block_size: {}", DataSize.ofBytes(fileSize).toMegabytes(), DataSize.ofBytes(fileSize), receivedLength);
        }
    }
}
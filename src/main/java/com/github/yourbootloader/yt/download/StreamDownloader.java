package com.github.yourbootloader.yt.download;

import com.github.yourbootloader.config.YDProperties;
import io.netty.handler.codec.http.HttpHeaders;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClientConfig;
import org.asynchttpclient.Dsl;
import org.asynchttpclient.handler.resumable.ResumableAsyncHandler;
import org.asynchttpclient.handler.resumable.ResumableListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.unit.DataSize;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class StreamDownloader {
    private static final int DEFAULT_TIMEOUT = 600_000;
    private static final DefaultAsyncHttpClientConfig clientConfig;
    static {
        clientConfig = new DefaultAsyncHttpClientConfig.Builder()
                .setRequestTimeout(DEFAULT_TIMEOUT)
                .setReadTimeout(DEFAULT_TIMEOUT)
                .setConnectTimeout(DEFAULT_TIMEOUT)
                .build();
    }

    private final YDProperties ydProperties;
    private final TempFileGenerator tempFileGenerator;

    // TODO вынести
    private String url;
    private String fileName;
    private Long fileSize;
    private HttpHeaders headers;
    private DownloadContext context;

    private void establishConnection() {
    }

    @SneakyThrows
    private File download() {
        File file = tempFileGenerator.create(fileName);

        validate(file);

        execute(file, clientConfig);
        return file;
    }

    private void validate(File file) {
        if (DataSize.ofBytes(fileSize).toMegabytes() > ydProperties.getMaxFileSize() ||
                DataSize.ofBytes(file.length()).toMegabytes() > ydProperties.getMaxFileSize()) {
            throw new RuntimeException("Лимит на скачивание файла превысил " + ydProperties.getMaxFileSize() + " Mb.");
        }
    }

    private void execute(File file, DefaultAsyncHttpClientConfig clientConfig) throws IOException, InterruptedException, ExecutionException {
        AtomicLong prevFileSize = new AtomicLong(Files.size(file.toPath()));
        try (AsyncHttpClient client = Dsl.asyncHttpClient(clientConfig)) {
            final RandomAccessFile resumeFile = new RandomAccessFile(file, "rw");
            ResumableAsyncHandler a = new ResumableAsyncHandler();
            a.setResumableListener(new ResumableListener() {

                public void onBytesReceived(ByteBuffer byteBuffer) throws IOException {
                    resumeFile.seek(resumeFile.length());
                    resumeFile.write(byteBuffer.array());
                    printContentWritten();
                }

                @SneakyThrows
                public void onAllBytesReceived() {
                    log.info("Файл полностью загружен.");
                    resumeFile.close();
                }

                @SneakyThrows
                public long length() {
                    return fileSize;
                }

                @SneakyThrows
                private void printContentWritten() {
                    long fileSize = Files.size(file.toPath());

                    if (DataSize.ofBytes(resumeFile.length()).toMegabytes() > ydProperties.getMaxFileSize()) {
                        throw new RuntimeException("Лимит на скачивание файла превысил " + ydProperties.getMaxFileSize() + " Mb.");
                    }

                    if (fileSize < 1_024) {
                        log.info("{} B ({} Kb) speed: {}", DataSize.ofBytes(fileSize), DataSize.ofBytes(fileSize), calcSpeed(fileSize));
                    } else if (fileSize < 1_048_576) {
                        log.info("{} Kb ({} Kb) speed: {}", DataSize.ofBytes(fileSize).toKilobytes(), DataSize.ofBytes(fileSize), calcSpeed(fileSize));
                    } else {
                        log.info("{} Mb ({} Kb) speed: {}", DataSize.ofBytes(fileSize).toMegabytes(), DataSize.ofBytes(fileSize), calcSpeed(fileSize));
                    }
                    prevFileSize.set(fileSize);
                }

                private String calcSpeed(long fileSize) {
                    DataSize dataSize = DataSize.ofBytes(fileSize - prevFileSize.get());
                    if (dataSize.toKilobytes() == 0) {
                        return dataSize.toBytes() + " Bytes";
                    }
                    return dataSize.toKilobytes() + " Kb (" + dataSize.toBytes() + " Bytes)";
                }
            });

            client.prepareGet(url).execute(a).get();
        }
    }

    public File realDownload(int retries, String url, String fileName, Long fileSize, HttpHeaders headers) throws Exception {
        this.url = url;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.headers = headers;

        context = DownloadContext.builder()
                .dataLen(0L)
                .resumeLen(0L) // TODO надо вычислять
                .blockSize(1024L) // TODO надо вычислять
                .chunkSize(1024L) // TODO надо вычислять
                .fileName(fileName)
                .tmpFileName(UUID.randomUUID() + ".mp3")
                .build();

        if (headers != null) {
            headers.add("Youtubedl-no-compression", "True");
        }

        int count = 0;

        while (count <= retries) {
            try {
                establishConnection();
                File downloadFile = download();
                log.info("Download finished...");
                return downloadFile;
            } catch (Exception exc) {
                count++;
                if (count <= retries) {
                    log.error("Exit downloading by error. Attempt {} of {}.", count, retries);
                    exc.printStackTrace();
                    continue;
                }
                log.error("[download] Got server HTTP error: {}. Retrying (attempt {} of {})...", exc.getMessage(), count, retries);
                throw new Exception(exc);
            }
        }
        return null;
    }
}

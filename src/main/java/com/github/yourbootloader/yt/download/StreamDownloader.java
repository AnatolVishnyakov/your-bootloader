package com.github.yourbootloader.yt.download;

import com.github.yourbootloader.config.YDProperties;
import io.netty.handler.codec.http.HttpHeaders;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClientConfig;
import org.asynchttpclient.Dsl;
import org.asynchttpclient.filter.ThrottleRequestFilter;
import org.asynchttpclient.handler.resumable.ResumableIOExceptionFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.util.unit.DataSize;

import java.io.File;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class StreamDownloader {
    private static final int DEFAULT_TIMEOUT = 600_000;

    private final YDProperties ydProperties;
    private final TempFileGenerator tempFileGenerator;
    private final ApplicationEventPublisher publisher;

    // TODO вынести
    private String url;
    private String fileName;
    private Long fileSize;
    private HttpHeaders headers;
    private DownloadContext context;

    private void establishConnection() {
    }

    private File download() throws Exception {
        DataSize dataSize = DataSize.ofBytes(fileSize);
        log.info("Размер скачиваемого содержимого: {} Mb ({} Kb)", dataSize.toMegabytes(), dataSize.toKilobytes());
        File file = tempFileGenerator.create(fileName);

        validate(file);

        DefaultAsyncHttpClientConfig clientConfig = new DefaultAsyncHttpClientConfig.Builder()
                .setRequestTimeout(DEFAULT_TIMEOUT)
                .setReadTimeout(DEFAULT_TIMEOUT)
                .setConnectTimeout(DEFAULT_TIMEOUT)
                .setMaxRequestRetry(3)
                .setThreadPoolName(StreamDownloader.class.getSimpleName())
                .setHttpClientCodecMaxChunkSize(16_384)
                .addIOExceptionFilter(new ResumableIOExceptionFilter())
                .addRequestFilter(new ThrottleRequestFilter(10))
                .build();

        try (AsyncHttpClient client = Dsl.asyncHttpClient(clientConfig)) {
            DownloaderAsyncHandler downloaderAsyncHandler = new DownloaderAsyncHandler(file);
            downloaderAsyncHandler.setApplicationEventPublisher(publisher);
            client.prepareGet(url).setRangeOffset(file.length())
                    .execute(downloaderAsyncHandler).get();
        }
        return file;
    }

    private void validate(File file) {
        if (DataSize.ofBytes(fileSize).toMegabytes() > ydProperties.getMaxFileSize() ||
                DataSize.ofBytes(file.length()).toMegabytes() > ydProperties.getMaxFileSize() ||
                DataSize.ofBytes(fileSize - file.length() + file.length()).toMegabytes() > ydProperties.getMaxFileSize()) {
            throw new RuntimeException("Лимит на скачивание файла превысил " + ydProperties.getMaxFileSize() + " Mb.");
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

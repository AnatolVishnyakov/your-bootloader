package com.github.yourbootloader.yt.download;

import com.github.yourbootloader.config.YDProperties;
import io.micrometer.core.instrument.MeterRegistry;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpHeaders;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClientConfig;
import org.asynchttpclient.Dsl;
import org.asynchttpclient.handler.resumable.ResumableAsyncHandler;
import org.asynchttpclient.handler.resumable.ResumableListener;
import org.springframework.util.unit.DataSize;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Data
public class StreamDownloader {
    private static final int DEFAULT_TIMEOUT = 600_000;

    private final String url;
    private final String fileName;
    private final Map<String, Object> info;
    private DownloadContext context;
    private YDProperties ydProperties;
    private MeterRegistry meterRegistry;

    public StreamDownloader(String url, String fileName, Map<String, Object> info) {
        this.url = url;
        this.fileName = fileName;
        this.info = info;
    }

    private void establishConnection() {
    }

    @SneakyThrows
    private File download() {
        File file = ydProperties.getDownloadPath().resolve(fileName.trim().replaceAll("\\W+", "-")).toFile();
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        long filesize = ((Integer) info.get("filesize")).longValue();
        long actualFileSize = file.length();
        if (DataSize.ofBytes(filesize).toMegabytes() > ydProperties.getMaxFileSize() ||
                DataSize.ofBytes(actualFileSize).toMegabytes() > ydProperties.getMaxFileSize()) {
            throw new RuntimeException("Лимит на скачивание файла превысил " + ydProperties.getMaxFileSize() + " Mb.");
        }

        DefaultAsyncHttpClientConfig clientConfig = new DefaultAsyncHttpClientConfig.Builder()
                .setRequestTimeout(DEFAULT_TIMEOUT)
                .setReadTimeout(DEFAULT_TIMEOUT)
                .setConnectTimeout(DEFAULT_TIMEOUT)
                .build();

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
                    return Long.valueOf(((Integer) info.get("filesize")));
                }

                @SneakyThrows
                private void printContentWritten() {
                    long fileSize = Files.size(file.toPath());

                    if (DataSize.ofBytes(resumeFile.length()).toMegabytes() > ydProperties.getMaxFileSize()) {
                        throw new RuntimeException("Лимит на скачивание файла превысил " + ydProperties.getMaxFileSize() + " Mb.");
                    }

                    if (fileSize < 1_024) {
                        log.info("{} B ({} Kb)", DataSize.ofBytes(fileSize), DataSize.ofBytes(fileSize));
                    } else if (fileSize < 1_048_576) {
                        log.info("{} Kb ({} Kb)", DataSize.ofBytes(fileSize).toKilobytes(), DataSize.ofBytes(fileSize));
                    } else {
                        log.info("{} Mb ({} Kb)", DataSize.ofBytes(fileSize).toMegabytes(), DataSize.ofBytes(fileSize));
                    }
                }
            });

            client.prepareGet(url).execute(a).get();
        }
        return file;
    }

    public File realDownload(int retries) throws Exception {
        context = DownloadContext.builder()
                .dataLen(0L)
                .resumeLen(0L) // TODO надо вычислять
                .blockSize(1024L) // TODO надо вычислять
                .chunkSize(1024L) // TODO надо вычислять
                .fileName(fileName)
                .tmpFileName(UUID.randomUUID() + ".mp3")
                .build();

        HttpHeaders headers = (DefaultHttpHeaders) info.get("http_headers");
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

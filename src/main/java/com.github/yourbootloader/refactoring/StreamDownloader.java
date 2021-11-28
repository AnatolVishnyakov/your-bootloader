package com.github.yourbootloader.refactoring;

import io.micrometer.core.instrument.MeterRegistry;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpHeaders;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.asynchttpclient.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.unit.DataSize;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.util.Map;
import java.util.UUID;

@Slf4j
public class StreamDownloader {
    private static final int DEFAULT_TIMEOUT = 600_000;

    private final String url;
    private final String fileName;
    private final Map<String, Object> info;
    private DownloadContext context;

    @Autowired
    MeterRegistry meterRegistry;

    public StreamDownloader(String url, String fileName, Map<String, Object> info) {
        this.url = url;
        this.fileName = fileName;
        this.info = info;
    }

    private void establishConnection() {
    }

    @SneakyThrows
    private void download() {
        FileOutputStream outputStream = new FileOutputStream(context.getAbsolutePath());

        DefaultAsyncHttpClientConfig clientConfig = new DefaultAsyncHttpClientConfig.Builder()
                .setRequestTimeout(DEFAULT_TIMEOUT)
                .setReadTimeout(DEFAULT_TIMEOUT)
                .setConnectTimeout(DEFAULT_TIMEOUT)
                .build();

        AsyncHttpClient client = Dsl.asyncHttpClient(clientConfig);
        client.prepareGet(url)
                .setHeaders(((HttpHeaders) info.get("http_headers")))
                .execute(new AsyncCompletionHandler<FileOutputStream>() {
                    @Override
                    public State onBodyPartReceived(HttpResponseBodyPart bodyPart) throws Exception {
                        printContentWritten();
                        outputStream.getChannel().write(bodyPart.getBodyByteBuffer());
                        return State.CONTINUE;
                    }

                    @Override
                    public FileOutputStream onCompleted(Response response) {
                        System.out.println("Completed download!");
                        return outputStream;
                    }

                    @SneakyThrows
                    private void printContentWritten() {
                        long fileSize = Files.size(new File(context.getAbsolutePath()).toPath());
                        DataSize dataSize = DataSize.ofBytes(fileSize);
                        if (dataSize.toMegabytes() == 0) {
                            log.info(dataSize.toKilobytes() + " Kb");
                        } else {
                    log.info(dataSize.toMegabytes() + " Mb (" + dataSize.toKilobytes() + " Kb)");
                }
            }
        }).get();
    }

    public void realDownload(int retries) {
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
                download();
            } catch (Exception exc) {
                count++;
                if (count <= retries) {
                    log.error("Exit downloading by error. Attempt {} of {}.", count, retries);
                    exc.printStackTrace();
                    return;
                }
                log.error("[download] Got server HTTP error: {}. Retrying (attempt {} of {})...", exc.getMessage(), count, retries);
                exc.printStackTrace();
            }
        }
    }
}

package com.github.yourbootloader.yt.download;

import com.github.yourbootloader.bot.event.FailureDownloadEvent;
import com.github.yourbootloader.bot.event.FinishDownloadEvent;
import com.github.yourbootloader.bot.event.ProgressIndicatorEvent;
import com.github.yourbootloader.config.YDProperties;
import io.netty.channel.AdaptiveRecvByteBufAllocator;
import io.netty.channel.ChannelOption;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.asynchttpclient.DefaultAsyncHttpClientConfig;
import org.asynchttpclient.filter.ThrottleRequestFilter;
import org.asynchttpclient.handler.resumable.ResumableIOExceptionFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.util.unit.DataSize;
import org.telegram.telegrambots.meta.api.objects.Chat;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class YtDownloadClient {
    private static final int DEFAULT_TIMEOUT = 10_000_000;

    private final YDProperties ydProperties;
    private final TempFileGenerator tempFileGenerator;
    private final ApplicationEventPublisher publisher;

    // TODO вынести
    private String url;
    private String fileName;
    private Long fileSize;
    private Chat chat;

    private void establishConnection() {
    }

    private void download() throws Exception {
        DataSize dataSize = DataSize.ofBytes(fileSize);
        log.info("Размер скачиваемого содержимого: {} Mb ({} Kb)", dataSize.toMegabytes(), dataSize.toKilobytes());
        File file = tempFileGenerator.create(fileName + "." + dataSize.toBytes());

        validate(file);

        DefaultAsyncHttpClientConfig clientConfig = new DefaultAsyncHttpClientConfig.Builder()
                .setRequestTimeout(DEFAULT_TIMEOUT)
                .setReadTimeout(DEFAULT_TIMEOUT)
                .setConnectTimeout(DEFAULT_TIMEOUT)
                .setMaxRequestRetry(3)
                .setThreadPoolName(YtDownloadClient.class.getSimpleName())
                .setHttpClientCodecMaxChunkSize(8_192 * 5)
                .setChunkedFileChunkSize(8_192 * 4)
                .addIOExceptionFilter(new ResumableIOExceptionFilter())
                .addRequestFilter(new ThrottleRequestFilter(1_000))
                .setIoThreadsCount(10)
                .setTcpNoDelay(true)
                .setKeepAlive(true)
                .setSoKeepAlive(true)
                .addChannelOption(ChannelOption.RCVBUF_ALLOCATOR, new AdaptiveRecvByteBufAllocator(8_192, 8_192 * 4, 131_072))
                .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/71.0.3578.25 Safari/537.36")
                .build();

//        DownloaderAsyncHandler downloaderAsyncHandler = new DownloaderAsyncHandler(chat, file);
//        downloaderAsyncHandler.setApplicationEventPublisher(publisher);
//        downloaderAsyncHandler.setContentSize(dataSize);
//        log.info("File length: {}", file.length());

//        try (AsyncHttpClient client = Dsl.asyncHttpClient(clientConfig)) {
//            DefaultHttpHeaders headers = new DefaultHttpHeaders();
//            headers.add("YtDownloader-no-compression", "True");
//            headers.add("Accept-Charset", "ISO-8859-1,utf-8;q=0.7,*;q=0.7");
//            headers.add("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
//            headers.add("Accept-Encoding", "gzip, deflate");
//            headers.add("Accept-Language", "en-us,en;q=0.5");

//            for (long start = 0, end = 0; end < dataSize.toBytes(); ) {
//                if (end + DataSize.ofKilobytes(500).toBytes() > dataSize.toBytes()) {
//                    end = dataSize.toBytes();
//                } else {
//                    end += DataSize.ofKilobytes(500).toBytes();
//                }
//                String range = "bytes=" + start + "-" + end;
//                log.info("Range: {}", range);
//                client.prepareGet(url)
//                        .setHeader("Range", range)
//                        .setHeaders(headers)
//                        .execute(downloaderAsyncHandler)
//                        .get();
//                start = end;
//            }

//            client.prepareGet(url)
//                    .setRangeOffset(file.length())
//                    .setHeaders(headers)
//                    .execute(downloaderAsyncHandler).get();
//        }
        UUID downloadId = UUID.randomUUID();
        try (BufferedInputStream in = new BufferedInputStream(new URL(url).openStream());
             FileOutputStream fileOutputStream = new FileOutputStream(file)) {
            int counter = 0;

            byte[] dataBuffer = new byte[1024];
            int bytesRead = in.read(dataBuffer, 0, 1024);
            long finish = 10_000;

            while (bytesRead != -1) {
                counter += dataBuffer.length;
                log.info("Downloaded {} of {} | chunk: {}", DataSize.ofBytes(counter).toKilobytes(), dataSize.toKilobytes(), dataBuffer.length);
                publisher.publishEvent(new ProgressIndicatorEvent(chat, dataSize, file.length(), dataBuffer.length, downloadId));
                fileOutputStream.write(dataBuffer, 0, bytesRead);

                long current = System.currentTimeMillis();
                if (current - finish > 200) {
                    dataBuffer = new byte[dataBuffer.length / 2];
                } else {
                    dataBuffer = new byte[dataBuffer.length * 2];
                }
                finish = current;
                bytesRead = in.read(dataBuffer, 0, dataBuffer.length);
            }
            publisher.publishEvent(new FinishDownloadEvent(chat, downloadId, file));
        } catch (IOException e) {
            // handle exception
            log.error("Error for download content: {}", url, e);
            publisher.publishEvent(new FailureDownloadEvent(chat, e));
        }
    }

    private void validate(File file) {
        if (DataSize.ofBytes(fileSize).toMegabytes() > ydProperties.getMaxFileSize() ||
                DataSize.ofBytes(file.length()).toMegabytes() > ydProperties.getMaxFileSize() ||
                DataSize.ofBytes(fileSize - file.length() + file.length()).toMegabytes() > ydProperties.getMaxFileSize()) {
            throw new RuntimeException("Лимит на скачивание файла превысил " + ydProperties.getMaxFileSize() + " Mb.");
        }
    }

    public void realDownload(int retries, String url, String fileName, Long fileSize) throws Exception {
        log.info("Скачивание url: {}", url);
        this.url = url;
        this.fileName = fileName;
        this.fileSize = fileSize;

        establishConnection();
        download();
        log.info("Download finished...");
    }

    public void setChat(Chat chat) {
        this.chat = chat;
    }
}

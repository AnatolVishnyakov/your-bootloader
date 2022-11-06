package com.github.yourbootloader.yt.download;

import com.github.yourbootloader.YoutubeDownloaderTest;
import com.github.yourbootloader.config.YDProperties;
import com.github.yourbootloader.yt.Utils;
import com.github.yourbootloader.yt.extractor.legacy.YoutubePageParser;
import io.netty.channel.AdaptiveRecvByteBufAllocator;
import io.netty.channel.ChannelOption;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.StreamEx;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.DefaultAsyncHttpClientConfig;
import org.asynchttpclient.Dsl;
import org.asynchttpclient.handler.TransferCompletionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.util.unit.DataSize;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@YoutubeDownloaderTest
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
class YtDownloadClientTest {

    // String ytUrl = "https://speed.hetzner.de/100MB.bin";
    private final YDProperties ydProperties;
    private final YtDownloadClient ytDownloadClient;
    private static final int CHUNK_SIZE = 10_485_760;
    private static final DefaultAsyncHttpClientConfig CLIENT_CONFIG = new DefaultAsyncHttpClientConfig.Builder()
            .setRequestTimeout(4_000_000) // 1.1 час
            .setReadTimeout(4_000_000)
            .setConnectTimeout(4_000_000)
            .setRequestTimeout(4_000_000)
            .setPooledConnectionIdleTimeout(4_000_000)
            .setHandshakeTimeout(4_000_000)
            .setChunkedFileChunkSize(CHUNK_SIZE)
            .setSoRcvBuf(CHUNK_SIZE)
            .setWebSocketMaxBufferSize(CHUNK_SIZE)
            .setWebSocketMaxFrameSize(CHUNK_SIZE)
            .setHttpClientCodecMaxChunkSize(CHUNK_SIZE)
            .setHttpClientCodecMaxHeaderSize(CHUNK_SIZE)
            .setHttpClientCodecMaxInitialLineLength(CHUNK_SIZE)
            .setHttpClientCodecInitialBufferSize(CHUNK_SIZE)
            .addChannelOption(ChannelOption.RCVBUF_ALLOCATOR, AdaptiveRecvByteBufAllocator.DEFAULT)
            .build();

    @TempDir
    Path tempDir;
    private File resource;

    @BeforeEach
    @SneakyThrows
    public void init() {
        ydProperties.setDownloadPath(tempDir.toString());

        String url = "https://youtu.be/zcjKJ7FHDLM";
        String fileAbsolutePath = "/Users/vishnyakov-ap/IdeaProjects/your-bootloader/src/test/resources/yt-url.txt";
        resource = new File(fileAbsolutePath);

        if (!resource.exists() && resource.createNewFile()) {
            YoutubePageParser youtubePageParser = new YoutubePageParser(url);
            List<Map<String, Object>> formats = youtubePageParser.parse();
            Map<String, Object> format = StreamEx.of(formats)
                    .findFirst(fmt -> fmt.get("ext").equals("m4a"))
                    .orElse(null);

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileAbsolutePath))) {
                writer.write(((String) Objects.requireNonNull(format).get("url")));
            }
        }
    }

    @Test
    @SneakyThrows
    void downloadSpeedDebug() {
        String ytUrl = Files.readAllLines(resource.toPath()).get(0);
        int chunkSizeDefault = 10_485_760;
        int chunkSize = ThreadLocalRandom.current().nextInt((int) (chunkSizeDefault * 0.95), chunkSizeDefault);

        try (AsyncHttpClient client = Dsl.asyncHttpClient(CLIENT_CONFIG)) {
            TransferCompletionHandler tl = new TransferCompletionHandler();
            tl.addTransferListener(new CustomTransferListener());
            client.prepareGet(ytUrl)
                    .setHeaders(Utils.newHttpHeaders())
                    .setHeader("Range", "bytes=0-" + chunkSize)
                    .execute(tl)
                    .get();
        }
    }

    @Test
    @SneakyThrows
    void downloadSpeedDebugNoYtUrl() {
        String ytUrl = "https://speed.hetzner.de/100MB.bin";
        try (AsyncHttpClient client = Dsl.asyncHttpClient(CLIENT_CONFIG)) {
            TransferCompletionHandler tl = new TransferCompletionHandler();

            tl.addTransferListener(new CustomTransferListener());
//            client.prepareGet(ytUrl).execute(tl);
//            client.prepareGet(ytUrl).execute(tl);
            client.prepareGet(ytUrl)
//                    .setHeader("Range", "bytes=0-" + DataSize.ofMegabytes(1).toBytes())
                    .execute(tl)
                    .get();
        }
    }

    @Test
    @SneakyThrows
    void downloadBlockedIO() {
        int chunkSizeDefault = 10_485_760;
        int chunkSize = ThreadLocalRandom.current().nextInt((int) (chunkSizeDefault * 0.95), chunkSizeDefault);
        HttpHeaders headers = Utils.newHttpHeaders();

        String ytUrl = Files.readAllLines(resource.toPath()).get(0);
        String fileName = "/Users/vishnyakov-ap/IdeaProjects/your-bootloader/src/test/resources/buffer.mp3";
        URL url = new URL(ytUrl);

        URLConnection urlConnection = url.openConnection();
        headers.keySet().forEach(key -> {
            urlConnection.addRequestProperty(key, headers.getFirst(key));
        });
        urlConnection.setRequestProperty("Range", "bytes=0-" + chunkSize);

        try (BufferedInputStream in = new BufferedInputStream(url.openStream());
             FileOutputStream fileOutputStream = new FileOutputStream(fileName)) {
            byte[] dataBuffer = new byte[8_192 * 2];
            int bytesRead;
            int counter = 0;

            long prev = System.currentTimeMillis();
            while ((bytesRead = in.read(dataBuffer, 0, dataBuffer.length)) != -1) {
                long current = System.currentTimeMillis();

                counter += bytesRead;
                log.info("Bytes read {} of {} [chunkSize = {}, time = {}ms]",
                        DataSize.ofBytes(counter).toKilobytes(),
                        DataSize.ofBytes(6245604).toKilobytes(),
                        bytesRead, current - prev
                );
                fileOutputStream.write(dataBuffer, 0, bytesRead);
                if (current - prev > 100) {
                    dataBuffer = new byte[dataBuffer.length / 2];
                } else {
                    dataBuffer = new byte[dataBuffer.length * 2];
                }
                prev = current;
            }
        } catch (IOException e) {
            // handle exception
        }
    }

    @Test
    @SneakyThrows
    void debugYtDownloadClientHandler() {
        String ytUrl = Files.readAllLines(resource.toPath()).get(0);
        ytDownloadClient.realDownload(ytUrl, UUID.randomUUID() + ".mp3", DataSize.ofKilobytes(31_273).toBytes());
    }
}
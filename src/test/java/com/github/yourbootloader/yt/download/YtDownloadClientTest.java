package com.github.yourbootloader.yt.download;

import com.github.yourbootloader.YoutubeDownloaderTest;
import com.github.yourbootloader.config.YDProperties;
import com.github.yourbootloader.yt.extractor.legacy.YoutubePageParser;
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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@YoutubeDownloaderTest
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
class YtDownloadClientTest {

    //        String ytUrl = "https://speed.hetzner.de/100MB.bin";
    private final YDProperties ydProperties;
    private static final int CHUNK_SIZE = 8_192 * 2;
    private static final DefaultAsyncHttpClientConfig CLIENT_CONFIG = new DefaultAsyncHttpClientConfig.Builder()
            .setRequestTimeout(4_000_000) // 1.1 час
            .setChunkedFileChunkSize(CHUNK_SIZE)
            .setSoRcvBuf(CHUNK_SIZE)
            .setWebSocketMaxBufferSize(CHUNK_SIZE)
            .setWebSocketMaxFrameSize(CHUNK_SIZE)
            .setHttpClientCodecMaxChunkSize(CHUNK_SIZE)
            .setHttpClientCodecMaxHeaderSize(CHUNK_SIZE)
            .setHttpClientCodecMaxInitialLineLength(CHUNK_SIZE)
            .setHttpClientCodecInitialBufferSize(CHUNK_SIZE)
            .setTcpNoDelay(false)
            .setEnablewebSocketCompression(false)
            .setKeepAlive(true)
            .build();

    @TempDir
    Path tempDir;
    private File resource;

    @BeforeEach
    @SneakyThrows
    public void init() {
        ydProperties.setDownloadPath(tempDir.toString());

        String url = "https://youtu.be/zcjKJ7FHDLM";
        String fileAbsolutePath = "D:\\IdeaProjects\\your-bootloader\\src\\test\\resources\\trash\\yt-url.txt";
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
        try (AsyncHttpClient client = Dsl.asyncHttpClient(CLIENT_CONFIG)) {
            TransferCompletionHandler tl = new TransferCompletionHandler();

            tl.addTransferListener(new CustomTransferListener());
            client.prepareGet(ytUrl)
                    .execute(tl)
                    .get();
        }
    }
}
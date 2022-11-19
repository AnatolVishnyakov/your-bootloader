package com.github.yourbootloader.yt.download;

import com.github.yourbootloader.YoutubeDownloaderTest;
import com.github.yourbootloader.config.YDProperties;
import com.github.yourbootloader.yt.Utils;
import com.github.yourbootloader.yt.extractor.legacy.YoutubePageParser;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import one.util.streamex.StreamEx;
import org.asynchttpclient.AsyncHttpClient;
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
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@YoutubeDownloaderTest
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
class DownloaderAsyncHandlerTest {

    private final DownloaderAsyncHandler downloaderAsyncHandler;
    private final YDProperties ydProperties;

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
        resource.delete();

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

        try (AsyncHttpClient client = Dsl.asyncHttpClient()) {
            TransferCompletionHandler tl = new TransferCompletionHandler();
            tl.addTransferListener(new CustomTransferListener());
            client.prepareGet(ytUrl)
                    .setHeaders(Utils.newHttpHeaders())
                    .setHeader("Range", "bytes=0-" + chunkSize)
                    .execute(tl)
                    .get();
        }
    }
}
package com.github.yourbootloader.yt.download;

import com.github.yourbootloader.YoutubeDownloaderTest;
import com.github.yourbootloader.yt.Utils;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.handler.TransferCompletionHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockserver.configuration.Configuration;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.ConnectionOptions;
import org.mockserver.model.MediaType;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;

import static org.asynchttpclient.Dsl.asyncHttpClient;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

@Slf4j
@YoutubeDownloaderTest
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BasicDownloadClientTest {

    private static final int CHUNK_SIZE = 1024;
    private static final int SIZE = 1024 * 10;
    private static final byte[] content = new byte[SIZE];
    static {
        Arrays.fill(content, (byte) 1);
    }

    private ClientAndServer mockServer;
    private final int port = 1090;

    @BeforeEach
    @SneakyThrows
    void startMockServer() {
        Configuration configuration = new Configuration();
        mockServer = startClientAndServer(configuration, port);
        mockServer.when(
                request()
                        .withMethod("GET")
                        .withPath("/download")
        ).respond(
                response(new String(content))
                        .withConnectionOptions(
                                ConnectionOptions.connectionOptions()
                                        .withChunkSize(CHUNK_SIZE)
                        )
                        .withContentType(MediaType.ANY_AUDIO_TYPE)
        );
    }

    @AfterEach
    public void stopMockServer() {
        mockServer.stop();
    }

    @Test
    @SneakyThrows
    void download() {
        String ytUrl = "http://localhost:" + port + "/download";
        byte[] expectedContent = new byte[SIZE];
        try (AsyncHttpClient client = asyncHttpClient()) {
            TransferCompletionHandler tl = new TransferCompletionHandler();
            tl.addTransferListener(new CustomTransferListener(expectedContent));
            client.prepareGet(ytUrl)
                    .setHeaders(Utils.newHttpHeaders())
                    .setHeader("Range", "bytes=0-" + content.length)
                    .execute(tl)
                    .get();
        }

        assertEquals(content.length, expectedContent.length);
        assertArrayEquals(content, expectedContent);
    }
}

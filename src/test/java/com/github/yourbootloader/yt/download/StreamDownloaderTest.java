package com.github.yourbootloader.yt.download;

import com.github.yourbootloader.YoutubeDownloaderTest;
import com.github.yourbootloader.config.YDProperties;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.file.Path;

@YoutubeDownloaderTest
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
class StreamDownloaderTest {

    private final StreamDownloader streamDownloader;
    private final YDProperties ydProperties;

    @TempDir
    Path tempDir;

    @BeforeEach
    public void init() {
        ydProperties.setDownloadPath(tempDir.toString());
    }

    @Test
    void realDownload() throws Exception {
        streamDownloader.realDownload(
                3,
                "https://speed.hetzner.de/100MB.bin",
                "test-6656",
                17844736L,
                new DefaultHttpHeaders()
        );
    }
}
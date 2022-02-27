package com.github.yourbootloader.yt.extractor;

import com.github.yourbootloader.YoutubeDownloaderTest;
import com.github.yourbootloader.config.YDProperties;
import com.github.yourbootloader.yt.download.YtDownloadClient;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@YoutubeDownloaderTest
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
class YoutubeIETest {

    private final YoutubeIE youtubeIE;
    private final YDProperties ydProperties;
    private final YtDownloadClient ytDownloadClient;

    @Test
    @SneakyThrows
    void realExtract() {
        ydProperties.setDownloadPath("D:\\Trash");
        String url = "https://www.youtube.com/watch?v=OCJi5hVdiZU";
        Map<String, Object> info = youtubeIE.realExtract(url);

        Map<String, Object> format = ((List<Map<String, Object>>) info.get("formats")).stream()
                .findFirst()
                .orElse(Collections.emptyMap());
        String realUrl = (String) format.get("url");
        String filename = (String) info.get("title");
        Long filesize = ((Integer) format.get("filesize")).longValue();

        ytDownloadClient.realDownload(3, realUrl, filename, filesize);
    }
}
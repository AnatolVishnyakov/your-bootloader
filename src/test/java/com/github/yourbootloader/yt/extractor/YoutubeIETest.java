package com.github.yourbootloader.yt.extractor;

import com.github.yourbootloader.YoutubeDownloaderTest;
import com.github.yourbootloader.config.YDProperties;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

@YoutubeDownloaderTest
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
class YoutubeIETest {

    private final YoutubeIE youtubeIE;
    private final YDProperties ydProperties;

    @Test
    void realExtract() {
        ydProperties.setDownloadPath("D:\\Trash");
        String url = "https://www.youtube.com/watch?v=OCJi5hVdiZU";
        Map<String, Object> info = youtubeIE.realExtract(url);
    }
}
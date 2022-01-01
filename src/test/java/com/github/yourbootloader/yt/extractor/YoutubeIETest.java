package com.github.yourbootloader.yt.extractor;

import com.github.yourbootloader.YoutubeDownloaderTest;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

@YoutubeDownloaderTest
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
class YoutubeIETest {

    private final YoutubeIE youtubeIE;

    @Test
    void realExtract() {
        String url = "https://www.youtube.com/watch?v=V0hagz_8L3M";
        Map<String, Object> info = youtubeIE.realExtract(url);
    }
}
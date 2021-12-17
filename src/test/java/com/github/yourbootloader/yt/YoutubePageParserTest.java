package com.github.yourbootloader.yt;

import com.github.yourbootloader.YoutubeDownloaderTest;
import com.github.yourbootloader.yt.download.StreamDownloader;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@YoutubeDownloaderTest
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
class YoutubePageParserTest {

    private final StreamDownloader downloader;

    @Test
    void foo() throws Exception {
        String url = "https://youtu.be/zcjKJ7FHDLM";
        YoutubePageParser youtubePageParser = new YoutubePageParser(url);
        List<Map<String, Object>> formats = youtubePageParser.parse();
        Map<String, Object> format = formats.get(0);

        url = ((String) format.get("url"));
        String title = (String) format.get("title");
        downloader.realDownload(3, url, title, ((Integer) format.get("filesize")).longValue(), ((DefaultHttpHeaders) format.get("http_headers")));
    }
}
package com.github.yourbootloader.refactoring;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.unit.DataSize;
import org.springframework.web.client.RestTemplate;

import java.io.FileOutputStream;
import java.net.URI;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static java.lang.String.format;

@Slf4j
public class YoutubePageDownloader {
    private final RestTemplate restTemplate;
    private final YoutubeUrl url;
    private final Map<String, Object> disablePolymer = new HashMap<String, Object>() {{
        put("disable_polymer", true);
    }};

    public YoutubePageDownloader(String url) {
        this.url = new YoutubeUrl(url);
        this.restTemplate = new RestTemplate();
    }

    public HtmlPage downloadPage() {
        String protocol = url.extractProtocol();
        String videoId = url.extractVideoId();
        YoutubeUrl youtubeUrl = new YoutubeUrl(
                format("%s://www.youtube.com/watch?v=%s&gl=US&hl=en&has_verified=1&bpctr=9999999999", protocol, videoId)
        );

        youtubeUrl.addQuery(disablePolymer);

        ResponseEntity<String> response = restTemplate.getForEntity(youtubeUrl.getUrl(), String.class);
        if (response.getStatusCode() != HttpStatus.OK) {
            throw new RuntimeException("Request web page is error!");
        }

        if (youtubeUrl.getUrl() == null || youtubeUrl.getUrl().length() <= 0) {
            throw new RuntimeException("Url illegal state exception!");
        }

        return new HtmlPage(videoId, response.getBody(), youtubeUrl.getUrl());
    }

    @SneakyThrows
    public static void main(String[] args) {
        YoutubePageDownloader youtubePageDownloader = new YoutubePageDownloader("https://www.youtube.com/watch?v=C4MpzSMkinw");
        HtmlPage htmlPage = youtubePageDownloader.downloadPage();
        YoutubePageParser youtubePageParser = new YoutubePageParser(htmlPage);
        Map<String, String> result = youtubePageParser.parse();

        StreamDownloader streamDownloader = new StreamDownloader(result.get("url"));
        streamDownloader.download();
    }
}

package com.github.yourbootloader.refactoring;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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

    public HtmlPage download() {
        String protocol = url.getProtocol();
        String videoId = url.extractVideoId();
        YoutubeUrl youtubeUrl = new YoutubeUrl(
                format("%s://www.youtube.com/watch?v=%s&gl=US&hl=en&has_verified=1&bpctr=9999999999", protocol, videoId)
        );

        youtubeUrl.addQuery(disablePolymer);

        ResponseEntity<String> response = restTemplate.getForEntity(youtubeUrl.getUri(), String.class);
        if (response.getStatusCode() != HttpStatus.OK) {
            throw new RuntimeException("Request web page is error!");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.set("urlh", youtubeUrl.getUri());
        response = ResponseEntity.status(response.getStatusCode())
                .headers(response.getHeaders())
                .headers(headers)
                .body(response.getBody());

        List<String> urlh = response.getHeaders().get("urlh");
        if (Objects.requireNonNull(urlh).isEmpty() || urlh.size() != 1) {
            throw new RuntimeException("Urlh illegal state exception!");
        }

        response = ResponseEntity.status(response.getStatusCode())
                .headers(response.getHeaders())
                .body(response.getBody());

        return new HtmlPage(videoId, response.getBody(), response.getHeaders().get("urlh").get(1));
    }

    public static void main(String[] args) {
        YoutubePageDownloader youtubePageDownloader = new YoutubePageDownloader("https://www.youtube.com/watch?v=_zJrhqUBF5o");
        HtmlPage htmlPage = youtubePageDownloader.download();
        YoutubeJsonParser youtubeJsonParser = new YoutubeJsonParser(htmlPage);
        Map<String, String> result = youtubeJsonParser.parse();
        System.out.println(result);
    }
}

package com.github.yourbootloader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

import static java.lang.String.format;

public abstract class YoutubeBaseInfoExtractor {
    private static final Logger logger = LoggerFactory.getLogger(YoutubeBaseInfoExtractor.class);

    protected ResponseEntity<String> downloadWebPageHandle(String url, String videoId, String note) {
        Map<String, Object> query = new HashMap<String, Object>() {{
            put("disable_polymer", true);
        }};
        return requestWebPage(url, videoId, note, query);
    }

    private ResponseEntity<String> requestWebPage(String url, String videoId, String note, Map<String, Object> query) {
        // Note handle
        if (note == null) {
            reportDownloadWebPage(videoId);
        }

        // Header handle
        if (!query.isEmpty()) {
            url = updateUrlQuery(url, query);
        }

        final RestTemplate rest = new RestTemplate();
        final ResponseEntity<String> responseEntity = rest.getForEntity(url, String.class);
        final HttpHeaders headers = new HttpHeaders();
        headers.set("urlh", url);
        return ResponseEntity.status(responseEntity.getStatusCode())
                .headers(responseEntity.getHeaders())
                .headers(headers)
                .body(responseEntity.getBody());
    }

    private String updateUrlQuery(String url, Map<String, Object> query) {
        StringBuilder urlBuilder = new StringBuilder(url);
        for (String key : query.keySet()) {
            urlBuilder.append(format("&%s=%s", key, query.get(key)));
        }
        url = urlBuilder.toString();
        return url;
    }

    private void reportDownloadWebPage(String videoId) {
        logger.info(format("[youtube] %s: Downloading webpage", videoId));
    }
}

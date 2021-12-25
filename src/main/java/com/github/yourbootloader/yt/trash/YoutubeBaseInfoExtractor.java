package com.github.yourbootloader.yt.trash;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.String.format;

public abstract class YoutubeBaseInfoExtractor {
    private static final Logger logger = LoggerFactory.getLogger(YoutubeBaseInfoExtractor.class);

    protected ResponseEntity<String> downloadWebPageHandle(String url, String videoId, String note) {
        Map<String, Object> query = new HashMap<String, Object>() {{
            put("disable_polymer", true);
        }};
        final ResponseEntity<String> urlh = requestWebPage(url, videoId, note, query);
        if (urlh.getStatusCode() != HttpStatus.OK) {
            throw new RuntimeException("Request web page is error");
        }

        return webPageReadContent(urlh, url, videoId, note);
    }

    private ResponseEntity<String> webPageReadContent(ResponseEntity<String> urlh, String url, String videoId, String note) {
        final MediaType contentType = urlh.getHeaders().getContentType();
        final String webPage = urlh.getBody();
        final String encoding = guessEncodingFromContent(contentType, webPage);

        String content = null;
        try {
            final byte[] webPageBytes = webPage.getBytes(encoding);
//            content = new String(webPageBytes);
            content = webPage;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        checkBlocked(content);

        return ResponseEntity.status(urlh.getStatusCode())
                .headers(urlh.getHeaders())
                .body(content);
    }

    private void checkBlocked(String content) {
        // TODO Implements
    }

    private String guessEncodingFromContent(MediaType contentType, String webPage) {
        final String encoding = "[a-zA-Z0-9_.-]+/[a-zA-Z0-9_.-]+\\s*;\\s*charset=(.+)";
        final Matcher matcher = Pattern.compile(encoding).matcher(contentType.toString());
        if (matcher.find()) {
            return matcher.group(1);
        } else {
            throw new UnsupportedOperationException();
        }
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

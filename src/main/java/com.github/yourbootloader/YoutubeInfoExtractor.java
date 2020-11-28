package com.github.yourbootloader;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.lang.String.format;

public class YoutubeInfoExtractor extends YoutubeBaseInfoExtractor {
    private static final Logger logger = LoggerFactory.getLogger(YoutubeInfoExtractor.class);
    // https://www.youtube.com/watch?v=_zJrhqUBF5o
    private static final String YOUTUBE_URL_PATTERN = "^((?:https?://|//)(?:www\\.)?www\\.youtube\\.com/(?:watch|movie)(?:\\?|#!\\?)v=)([0-9A-Za-z_-]{11})$";
    private static final Pattern YOUTUBE_PATTERN = Pattern.compile(YOUTUBE_URL_PATTERN);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String url;

    public YoutubeInfoExtractor(String url) {
        this.url = url;
    }

    public void download() {
        for (int i = 0; i < 3; i++) {
            try {
                realExtract();
            } catch (Exception e) {
                logger.error("Retry download... reason: {}", e.getMessage());
                try {
                    Thread.sleep(2_000);
                } catch (InterruptedException exc) {
                    logger.error("Thread sleep exception", exc);
                }
            }
        }
    }

    public void realExtract() {
        String videoId = extractId();
        final String proto = url.startsWith("https")
                ? "https" : "http";
        final String urlVideoWebPage = format("%s://www.youtube.com/watch?v=%s&gl=US&hl=en&has_verified=1&bpctr=9999999999",
                proto, videoId);
        logger.info("Video page url: {}", urlVideoWebPage);

        final ResponseEntity<String> response = downloadWebPageHandle(urlVideoWebPage, videoId, null);
        final String urlh = Objects.requireNonNull(response.getHeaders().get("urlh")).get(0);
        String videoWebPage = response.getBody();

        Map<Object, String> query = null;
        try {
            final URI uri = new URI(urlh);
            query = Arrays.stream(uri.getQuery().split("&"))
                    .map(s -> s.split("="))
                    .collect(Collectors.toMap(s -> s[0], s -> s[1]));
        } catch (URISyntaxException e) {
            logger.error("URI created error", e);
        }

        videoId = query.getOrDefault("v", videoId);
        // TODO Not implemented: extract SWF player URL

        // Get video info
        getPlayerConfig(videoId, videoWebPage);
        System.out.println(query);
    }

    private JsonNode getPlayerConfig(String videoId, String videoWebPage) {
        Pattern[] patterns = {
                Pattern.compile(";ytplayer\\.config\\s*=\\s*(\\{.+?});ytplayer"),
                Pattern.compile(";ytplayer\\.config\\s*=\\s*(\\{.+?});")
        };

        String config = searchPattern(patterns, videoWebPage, "ytplayer.config");
        logger.info(config);

        if (config != null) {
            try {
                return objectMapper.readTree(config);
            } catch (JsonProcessingException e) {
                logger.error("Parsing error", e);
            }
        }

        throw new IllegalStateException("Config player is not found");
    }

    private String searchPattern(Pattern[] patterns, String videoWebPage, String player) {
        for (Pattern pattern : patterns) {
            final Matcher matcher = pattern.matcher(videoWebPage);
            if (matcher.find()) {
                return matcher.group(1);
            }
        }
        throw new IllegalStateException("Youtube player pattern not found");
    }

    private String extractId() {
        final Matcher matcher = YOUTUBE_PATTERN.matcher(url);
        if (matcher.find()) {
            return matcher.group(2);
        }
        throw new RuntimeException("Не удалось распарсить url");
    }

    public static void main(String[] args) {
        new YoutubeInfoExtractor("https://www.youtube.com/watch?v=_zJrhqUBF5o")
                .download();
    }
}

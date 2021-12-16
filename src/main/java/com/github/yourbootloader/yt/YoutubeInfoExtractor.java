package com.github.yourbootloader.yt;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.function.Function;
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
        for (int i = 0; i < 1; i++) {
            try {
                final Map<String, String> videoInfo = realExtract();
            } catch (NullPointerException e) {
                e.printStackTrace();
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

    public Map<String, String> realExtract() {
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
        final JsonNode youtubePlayerConfig = getPlayerConfig(videoId, videoWebPage);
        if (youtubePlayerConfig != null) {
            if (youtubePlayerConfig.has("args")) {
                final JsonNode args = youtubePlayerConfig.get("args");
                if (args.has("url_encoded_fmt_stream_map") || args.has("hlsvp")) {
                    logger.warn("Args has url_encoded_fmt_stream_map [NOT IMPLEMENTED HANDLER]");
                }

                if (args.has("player_response")) {
                    logger.warn("Args has player_response [NOT IMPLEMENTED HANDLER]");
                }
            }

            JsonNode videoDetails = youtubePlayerConfig.get("videoDetails");
            final JsonNode microformat = youtubePlayerConfig.get("microformat");
            final String videoTitle = videoDetails.get("title").asText();
            final String videoDescription = videoDetails.get("shortDescription").asText();
            final int viewCount = videoDetails.get("viewCount").asInt();

            final ArrayNode streamingFormats = ((ArrayNode) youtubePlayerConfig.get("streamingData").get("formats"));
            final ArrayNode streamingFormats2 = ((ArrayNode) youtubePlayerConfig.get("streamingData").get("adaptiveFormats"));
            streamingFormats.addAll(streamingFormats2);

            Map<String, Map<String, Object>> formatsSpec = new HashMap<>(streamingFormats.size());
            for (JsonNode fmt : streamingFormats) {
                final String itag = defaultOrValue(fmt.get("itag"), null, JsonNode::asText);
                final String quality = defaultOrValue(fmt.get("quality"), null, JsonNode::asText);
                final String qualityLabel = defaultOrValue(fmt.get("qualityLabel"), quality, JsonNode::asText);
                formatsSpec.put(itag, new HashMap<String, Object>() {{
                    put("asr", fmt.get("audioSampleRate"));
                    put("filesize", defaultOrValue(fmt.get("contentLength"), 0, JsonNode::asInt));
                    put("fps", defaultOrValue(fmt.get("fps"), 0, JsonNode::asInt));
                    put("height", defaultOrValue(fmt.get("height"), 0, JsonNode::asInt));
                    put("width", defaultOrValue(fmt.get("width"), 0, JsonNode::asInt));
                    put("format_note", qualityLabel);
                    final double bitrate = fmt.has("averageBitrate")
                            ? fmt.get("averageBitrate").asDouble()
                            : fmt.has("bitrate")
                            ? fmt.get("bitrate").asDouble()
                            : 1000;
                    put("tbr", bitrate);
                }});
            }

            Map<String, String> result = null;
            for (JsonNode fmt : streamingFormats) {
                final JsonNode url = fmt.get("url");
                if (url == null) {
                    throw new UnsupportedOperationException("Not imlemented url handler");
                } else {
                    try {
                        final URI uri = new URI(url.asText());
                        final Map<String, Object> urlData = Arrays.stream(uri.getQuery().split("&"))
                                .map(s -> s.split("="))
                                .collect(Collectors.toMap(s -> s[0], s -> s[1]));
                        final String formatId = fmt.has("itag")
                                ? fmt.get("itag").asText()
                                : ((String) urlData.get("itag"));

                        result = new HashMap<>();
                        result.put("format_id", formatId);
                        result.put("url", url.asText());
                        result.put("player_url", null); // TODO Implements player url handler
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    }
                }
            }
            
            return result;
        }
        return null;
    }

    private <T, R> R defaultOrValue(T value, R defaultValue, Function<T, R> function) {
        return Optional.ofNullable(value)
                .map(function)
                .orElse(defaultValue);
    }

    private JsonNode getPlayerConfig(String videoId, String videoWebPage) {
        Pattern[] patterns = {
                Pattern.compile("ytInitialPlayerResponse\\s*=\\s*(\\{.+?});"),
                Pattern.compile(";ytplayer\\.config\\s*=\\s*(\\{.+?});ytplayer"),
                Pattern.compile("ytplayer\\.config\\s*=\\s*(\\{.+?});")
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
}

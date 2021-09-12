package com.github.yourbootloader.refactoring;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
public class YoutubePageParser {
    private final HtmlPage htmlPage;
    private final ObjectMapper objectMapper;

    public YoutubePageParser(HtmlPage htmlPage) {
        this.htmlPage = htmlPage;
        this.objectMapper = new ObjectMapper();
    }

    public Map<String, String> parse() {
        String html = htmlPage.getHtml();

        Map<Object, String> query = null;
        try {
            final URI uri = new URI(htmlPage.getUrlh());
            query = Arrays.stream(uri.getQuery().split("&"))
                    .map(s -> s.split("="))
                    .collect(Collectors.toMap(s -> s[0], s -> s[1]));
        } catch (URISyntaxException e) {
            log.error("URI created error", e);
        }

        String videoId = Objects.requireNonNull(query).getOrDefault("v", htmlPage.getVideoId());

        // Get video info
        final JsonNode youtubePlayerConfig = getPlayerConfig(videoId, html);
        if (youtubePlayerConfig != null) {
            if (youtubePlayerConfig.has("args")) {
                final JsonNode args = youtubePlayerConfig.get("args");
                if (args.has("url_encoded_fmt_stream_map") || args.has("hlsvp")) {
                    log.warn("Args has url_encoded_fmt_stream_map [NOT IMPLEMENTED HANDLER]");
                }

                if (args.has("player_response")) {
                    log.warn("Args has player_response [NOT IMPLEMENTED HANDLER]");
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
        log.info(config);

        if (config != null) {
            try {
                return objectMapper.readTree(config);
            } catch (JsonProcessingException e) {
                log.error("Parsing error", e);
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
}

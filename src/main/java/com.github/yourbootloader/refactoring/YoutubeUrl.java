package com.github.yourbootloader.refactoring;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.String.format;

public class YoutubeUrl {
    private static final String YOUTUBE_URL_PATTERN = "^((?:https?://|//)(?:www\\.)?www\\.youtube\\.com/(?:watch|movie)(?:\\?|#!\\?)v=)([0-9A-Za-z_-]{11})$";
    private static final Pattern patternCompile = Pattern.compile(YOUTUBE_URL_PATTERN);

    private String url;

    public YoutubeUrl(String url) {
        this.url = url;
    }

    public String extractVideoId() {
        Matcher matcher = patternCompile.matcher(url);
        if (matcher.find()) {
            return matcher.group(2);
        }
        throw new RuntimeException("Не удалось распарсить url!");
    }

    public String extractProtocol() {
        return url.startsWith("https")
                ? "https" : "http";
    }

    public void addQuery(Map<String, Object> query) {
        StringBuilder urlBuilder = new StringBuilder(url);
        for (String key : query.keySet()) {
            urlBuilder.append(format("&%s=%s", key, query.get(key)));
        }
        url = urlBuilder.toString();
    }

    public String getUrl() {
        return url;
    }
}

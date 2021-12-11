package com.github.yourbootloader.refactoring;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.String.format;

public class YoutubeUrl {
    private static final String YOUTUBE_URL_PATTERN = "^((?:https?://|//)(?:www\\.)?www\\.youtube\\.com/(?:watch|movie)(?:\\?|#!\\?)v=)([0-9A-Za-z_-]{11})$";
    private static final Pattern patternCompile = Pattern.compile(YOUTUBE_URL_PATTERN);

    private String url;
    private String videoId;
    private String httpScheme;
    private String webPageUrl;

    public YoutubeUrl(String url) {
        this.url = url;
        this.videoId = parseVideoId();
        this.httpScheme = parseProtocol();
        this.webPageUrl = format("%s//www.youtube.com/watch?v=%s", httpScheme, videoId);
    }

    private String parseVideoId() {
        Matcher matcher = patternCompile.matcher(url);
        if (matcher.find()) {
            return matcher.group(2);
        }
        throw new RuntimeException("Не удалось распарсить url!");
    }

    private String parseProtocol() {
        // TODO анализ prefer_insecure
        return url.startsWith("https")
                ? "https" : "http";
    }

    public String getWebPageUrl() {
        return webPageUrl;
    }
}

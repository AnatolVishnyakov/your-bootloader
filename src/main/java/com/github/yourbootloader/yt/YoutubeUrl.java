package com.github.yourbootloader.yt;

import lombok.Value;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.String.format;

@Value
public class YoutubeUrl {
    private static final String YOUTUBE_URL_PATTERN = "^((?:https?://|//)(?:www\\.)?www\\.youtube\\.com/(?:watch|movie)(?:\\?|#!\\?)v=)([0-9A-Za-z_-]{11})$";
    private static final Pattern patternCompile = Pattern.compile(YOUTUBE_URL_PATTERN);
    private static String YOUTUBE_URL_PATTERNS = "" +
            "^((?:https?:)?\\/\\/)?((?:www|m)\\.)?((?:youtube\\.com|youtu.be))(\\/(?:[\\w\\-]+\\?v=|embed\\/|v\\/)?)([\\w\\-]+)(\\S+)?$";

    String url;
    String videoId;
    String httpScheme;
    String webPageUrl;

    public YoutubeUrl(String url) {
        this.url = url;
        this.videoId = parseVideoId();
        this.httpScheme = parseProtocol();
        this.webPageUrl = format("%s://www.youtube.com/watch?v=%s", httpScheme, videoId);
    }

    private String parseVideoId() {
        Matcher matcher = patternCompile.matcher(url);
        if (matcher.find()) {
            return matcher.group(2);
        } else {
            matcher = Pattern.compile(YOUTUBE_URL_PATTERNS).matcher(url);
            if (matcher.find()) {
                return matcher.group(2);
            }
        }
        throw new RuntimeException("Не удалось распарсить url!");
    }

    private String parseProtocol() {
        // TODO анализ prefer_insecure
        return url.startsWith("https")
                ? "https" : "http";
    }
}

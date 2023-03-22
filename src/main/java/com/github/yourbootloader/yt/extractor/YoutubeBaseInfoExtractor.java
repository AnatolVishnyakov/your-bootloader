package com.github.yourbootloader.yt.extractor;

import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;

import static java.lang.String.format;

/**
 * Provide base functions for Youtube extractors
 */
@Slf4j
public abstract class YoutubeBaseInfoExtractor extends InfoExtractor {

    public static String _YT_INITIAL_DATA_RE = "(?:window\\s*\\[\\s*[\"']ytInitialData[\"']\\s*\\]|ytInitialData)\\s*=\\s*(\\{.+?})\\s*;";
    public static String _YT_INITIAL_PLAYER_RESPONSE_RE = "ytInitialPlayerResponse\\s*=\\s*(\\{.+?})\\s*;";
    public static String _YT_INITIAL_BOUNDARY_RE = "(?:var\\s+meta|</script|\n)";

    @Autowired
    protected YoutubeBaseInfoExtractor(YoutubeDLService youtubeDLService) {
        super(youtubeDLService);
    }

    public JSONObject extractYtInitialData(String videoId, String webPage) {
        return this.parseJson(
                this.searchRegex(
                        Collections.singletonList(format("%s\\s*%s %s", _YT_INITIAL_DATA_RE, _YT_INITIAL_BOUNDARY_RE, _YT_INITIAL_DATA_RE)), // TODO check pattern count
                        webPage,
                        "yt initial data"
                ),
                videoId
        );
    }

    public JSONObject extractYtcfg(String videoId, String webPage) {
        return this.parseJson(
                this.searchRegex(
                        Collections.singletonList("ytcfg\\.set\\s*\\(\\s*({.+?})\\s*\\)\\s*;"), // TODO check pattern count
                        webPage,
                        "ytcfg"
                ),
                videoId
        );
    }
}

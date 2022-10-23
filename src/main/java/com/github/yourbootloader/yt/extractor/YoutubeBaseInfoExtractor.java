package com.github.yourbootloader.yt.extractor;

import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.regex.Pattern;

import static java.lang.String.format;

/**
 * Provide base functions for Youtube extractors
 */
@Slf4j
public abstract class YoutubeBaseInfoExtractor extends InfoExtractor {
    public static String _LOGIN_URL = "https://accounts.google.com/ServiceLogin";
    public static String _TWOFACTOR_URL = "https://accounts.google.com/signin/challenge";
    public static String _LOOKUP_URL = "https://accounts.google.com/_/signin/sl/lookup";
    public static String _CHALLENGE_URL = "https://accounts.google.com/_/signin/sl/challenge";
    public static String _TFA_URL = "https://accounts.google.com/_/signin/challenge?hl=en&TL={0}";
    public static String _NETRC_MACHINE = "youtube";
    // If True it will raise an error if no login info is provided
    public static boolean _LOGIN_REQUIRED = false;
    public static Pattern _PLAYLIST_ID_RE = Pattern.compile("(?:(?:PL|LL|EC|UU|FL|RD|UL|TL|PU|OLAK5uy_)[0-9A-Za-z-_]{10,}|RDMM)");

    public static JSONObject _DEFAULT_API_DATA = new JSONObject(
            "{" +
                    "    'context': {" +
                    "        'client': {" +
                    "            'clientName': 'WEB'," +
                    "                    'clientVersion': '2.20201021.03.00'," +
                    "        }" +
                    "    }," +
                    "}"
    );
    public static String _YT_INITIAL_DATA_RE = "(?:window\\s*\\[\\s*[\"\']ytInitialData[\"\']\\s*\\]|ytInitialData)\\s*=\\s*(\\{.+?})\\s*;";
    public static String _YT_INITIAL_PLAYER_RESPONSE_RE = "ytInitialPlayerResponse\\s*=\\s*(\\{.+?})\\s*;";
    public static String _YT_INITIAL_BOUNDARY_RE = "(?:var\\s+meta|</script|\n)";

    @Autowired
    protected YoutubeBaseInfoExtractor(YoutubeDLService youtubeDLService) {
        super(youtubeDLService);
    }

    // TODO реализовать авторизацию
    public boolean login() {
        log.error("Not implemented!");
        throw new RuntimeException();
    }

    // TODO реализовать работу с куками
    public void initializeConsent() {
        log.error("Not implemented!");
        throw new RuntimeException();
    }

    // TODO implement
    public void realInitialize() {
        initializeConsent();
        if (this.downloader == null || !login()) {
            return;
        }
    }

    public void callApi() {
        log.error("Not implemented!");
        throw new RuntimeException();
    }

    public JSONObject extractYtInitialData(String videoId, String webPage) {
        return this.parseJson(
                this.searchRegex(
                        Arrays.asList(format("%s\\s*%s %s", _YT_INITIAL_DATA_RE, _YT_INITIAL_BOUNDARY_RE, _YT_INITIAL_DATA_RE)), // TODO check pattern count
                        webPage,
                        "yt initial data"
                ),
                videoId
        );
    }

    public JSONObject extractYtcfg(String videoId, String webPage) {
        return this.parseJson(
                this.searchRegex(
                        Arrays.asList("ytcfg\\.set\\s*\\(\\s*({.+?})\\s*\\)\\s*;"), // TODO check pattern count
                        webPage,
                        "ytcfg"
                ),
                videoId
        );
    }

    public void extractVideo() {
        log.error("Not implemented!");
        throw new RuntimeException();
    }
}

package com.github.yourbootloader.yt.extractor;

import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.regex.Pattern;

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
    public static Pattern _YT_INITIAL_DATA_RE = Pattern.compile("(?:window\\s*\\[\\s*[\"\']ytInitialData[\"\']\\s*\\]|ytInitialData)\\s*=\\s*(\\{.+?})\\s*;");
    public static Pattern _YT_INITIAL_PLAYER_RESPONSE_RE = Pattern.compile("ytInitialPlayerResponse\\s*=\\s*(\\{.+?})\\s*;");
    public static Pattern _YT_INITIAL_BOUNDARY_RE = Pattern.compile("(?:var\\s+meta|</script|\n)");

    @Autowired
    protected YoutubeBaseInfoExtractor(YoutubeDLService youtubeDLService) {
        super(youtubeDLService);
    }

    // TODO реализовать авторизацию
    public boolean login() {
        log.warn("Not implemented!");
        return false;
    }

    // TODO реализовать работу с куками
    public void initializeConsent() {
        log.warn("Not implemented!");
    }

    // TODO implement
    public void realInitialize() {
        initializeConsent();
        if (this.downloader == null || !login()) {
            return;
        }
        if (!login()) {
            return;
        }
    }

    public void callApi() {
        log.warn("Not implemented!");
    }
}

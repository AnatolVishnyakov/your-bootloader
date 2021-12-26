package com.github.yourbootloader.yt.extractor;

import com.github.yourbootloader.yt.exception.MethodNotImplementedException;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.String.format;

@Slf4j
@Service
public class YoutubeIE extends YoutubeBaseInfoExtractor {

    private static String IE_DESC = "YouTube.com";
    private static List<String> _INVIDIOUS_SITES = Arrays.asList(
            // invidious-redirect websites
            "(?:www\\.)?redirect\\.invidious\\.io",
            "(?:(?:www|dev)\\.)?invidio\\.us",
            // Invidious instances taken from
            // https://github.com/iv-org/documentation/blob/master/Invidious-Instances.md
            "(?:(?:www|no)\\.)?invidiou\\.sh",
            "(?:(?:www|fi)\\.)?invidious\\.snopyta\\.org",
            "(?:www\\.)?invidious\\.kabi\\.tk",
            "(?:www\\.)?invidious\\.13ad\\.de",
            "(?:www\\.)?invidious\\.mastodon\\.host",
            "(?:www\\.)?invidious\\.zapashcanon\\.fr",
            "(?:www\\.)?(?:invidious(?:-us)?|piped)\\.kavin\\.rocks",
            "(?:www\\.)?invidious\\.tinfoil-hat\\.net",
            "(?:www\\.)?invidious\\.himiko\\.cloud",
            "(?:www\\.)?invidious\\.reallyancient\\.tech",
            "(?:www\\.)?invidious\\.tube",
            "(?:www\\.)?invidiou\\.site",
            "(?:www\\.)?invidious\\.site",
            "(?:www\\.)?invidious\\.xyz",
            "(?:www\\.)?invidious\\.nixnet\\.xyz",
            "(?:www\\.)?invidious\\.048596\\.xyz",
            "(?:www\\.)?invidious\\.drycat\\.fr",
            "(?:www\\.)?inv\\.skyn3t\\.in",
            "(?:www\\.)?tube\\.poal\\.co",
            "(?:www\\.)?tube\\.connect\\.cafe",
            "(?:www\\.)?vid\\.wxzm\\.sx",
            "(?:www\\.)?vid\\.mint\\.lgbt",
            "(?:www\\.)?vid\\.puffyan\\.us",
            "(?:www\\.)?yewtu\\.be",
            "(?:www\\.)?yt\\.elukerio\\.org",
            "(?:www\\.)?yt\\.lelux\\.fi",
            "(?:www\\.)?invidious\\.ggc-project\\.de",
            "(?:www\\.)?yt\\.maisputain\\.ovh",
            "(?:www\\.)?ytprivate\\.com",
            "(?:www\\.)?invidious\\.13ad\\.de",
            "(?:www\\.)?invidious\\.toot\\.koeln",
            "(?:www\\.)?invidious\\.fdn\\.fr",
            "(?:www\\.)?watch\\.nettohikari\\.com",
            "(?:www\\.)?invidious\\.namazso\\.eu",
            "(?:www\\.)?invidious\\.silkky\\.cloud",
            "(?:www\\.)?invidious\\.exonip\\.de",
            "(?:www\\.)?invidious\\.riverside\\.rocks",
            "(?:www\\.)?invidious\\.blamefran\\.net",
            "(?:www\\.)?invidious\\.moomoo\\.de",
            "(?:www\\.)?ytb\\.trom\\.tf",
            "(?:www\\.)?yt\\.cyberhost\\.uk",
            "(?:www\\.)?kgg2m7yk5aybusll\\.onion",
            "(?:www\\.)?qklhadlycap4cnod\\.onion",
            "(?:www\\.)?axqzx4s6s54s32yentfqojs3x5i7faxza6xo3ehd4bzzsg2ii4fv2iid\\.onion",
            "(?:www\\.)?c7hqkpkpemu6e7emz5b4vyz7idjgdvgaaa3dyimmeojqbgpea3xqjoid\\.onion",
            "(?:www\\.)?fz253lmuao3strwbfbmx46yu7acac2jz27iwtorgmbqlkurlclmancad\\.onion",
            "(?:www\\.)?invidious\\.l4qlywnpwqsluw65ts7md3khrivpirse744un3x7mlskqauz5pyuzgqd\\.onion",
            "(?:www\\.)?owxfohz4kjyv25fvlqilyxast7inivgiktls3th44jhk3ej3i7ya\\.b32\\.i2p",
            "(?:www\\.)?4l2dgddgsrkf2ous66i6seeyi6etzfgrue332grh2n7madpwopotugyd\\.onion",
            "(?:www\\.)?w6ijuptxiku4xpnnaetxvnkc5vqcdu7mgns2u77qefoixi63vbvnpnqd\\.onion",
            "(?:www\\.)?kbjggqkzv65ivcqj6bumvp337z6264huv5kpkwuv6gu5yjiskvan7fad\\.onion",
            "(?:www\\.)?grwp24hodrefzvjjuccrkw3mjq4tzhaaq32amf33dzpmuxe7ilepcmad\\.onion",
            "(?:www\\.)?hpniueoejy4opn7bc4ftgazyqjoeqwlvh2uiku2xqku6zpoa4bf5ruid\\.onion"
    );
    private static String _VALID_URL = ("(?x)^" +
            "(" +
            "    (?:https?://|//)" +                                                              // http(s):// or protocol-independent URL
            "    (?:(?:(?:(?:\\w+\\.)?[yY][oO][uU][tT][uU][bB][eE](?:-nocookie|kids)?\\.com|" +
            "       (?:www\\.)?deturl\\.com/www\\.youtube\\.com|" +
            "       (?:www\\.)?pwnyoutube\\.com|" +
            "       (?:www\\.)?hooktube\\.com|" +
            "       (?:www\\.)?yourepeat\\.com|" +
            "       tube\\.majestyc\\.net|" +
            "       %\\{invidious}|" +
            "       youtube\\.googleapis\\.com)/" +                                               // the various hostnames, with wildcard subdomains
            "    (?:.*?\\#/)?" +                                                                  // handle anchor (#/) redirect urls
            "    (?:" +                                                                           // the various things that can precede the ID:
            "        (?:(?:v|embed|e)/(?!videoseries))" +                                         // v/ or embed/ or e/
            "        |(?:" +                                                                      // or the v= param in all its forms
            "            (?:(?:watch|movie)(?:_popup)?(?:\\.php)?/?)?" +                          // preceding watch(_popup|.php) or nothing (like /?v=xxxx)
            "            (?:\\?|\\#!?)" +                                                         // the params delimiter ? or # or #!
            "            (?:.*?[&;])??" +                                                         // any other preceding param (like /?s=tuff&v=xxxx or ?s=tuff&amp;v=V36LpHqtcDY)
            "            v=" +
            "        )" +
            "    ))" +
            "    |(?:" +
            "       youtu\\.be|" +                                                                // just youtu.be/xxxx
            "       vid\\.plus|" +                                                                // or vid.plus/xxxx
            "       zwearz\\.com/watch|" +                                                        // or zwearz.com/watch/xxxx
            "       %\\{invidious}" +
            "    )/" +
            "    |(?:www\\.)?cleanvideosearch\\.com/media/action/yt/watch\\?videoId=" +
            "    )" +
            ")?" +                                                                                // all until now is optional -> you can pass the naked ID
//            "(?P<id>[0-9A-Za-z_-]{11})" +                                                         // here is it! the YouTube video ID
//            "(?(1).+)?" +                                                                         // if we found the ID, everything can follow
            "$").replaceAll("%\\{invidious}", String.join("|", _INVIDIOUS_SITES));
    private static List<String> _PLAYER_INFO_RE = Arrays.asList(
            "/s/player/(?P<id>[a-zA-Z0-9_-]{8,})/player",
            "/(?P<id>[a-zA-Z0-9_-]{8,})/player(?:_ias\\.vflset(?:/[a-zA-Z]{2,3}_[a-zA-Z]{2,3})?|-plasma-ias-(?:phone|tablet)-[a-z]{2}_[A-Z]{2}\\.vflset)/base\\.js$",
            "\b(?P<id>vfl[a-zA-Z0-9_-]+)\b.*?\\.js$"
    );
    private static List<String> _SUBTITLE_FORMATS = Arrays.asList("srv1", "srv2", "srv3", "ttml", "vtt");
    private static boolean _GEO_BYPASS = false;
    private static String IE_NAME = "youtube";
    private final Map<Object, Object> codeCache;
    private final Map<Object, Object> playerCache;

    @Autowired
    protected YoutubeIE(YoutubeDLService youtubeDLService) {
        super(youtubeDLService);
        this.codeCache = new HashMap<>();
        this.playerCache = new HashMap<>();
    }

    public boolean suitable(String url) {
        log.warn("Not implemented!");
        // TODO implements super.suitable(url)
        Pattern pattern = Pattern.compile(_VALID_URL);
        Matcher matcher = pattern.matcher(url);
        return matcher.matches();
    }

    public void _signature_cache_id(String exampleSig) {
        throw new MethodNotImplementedException();
    }

    public void extractPlayerInfo(String playerUrl) {
        throw new MethodNotImplementedException();
    }

    public void _extract_signature_function(String videoId, String playerUrl, String exampleSig) {
        throw new MethodNotImplementedException();
    }

    public void printSigCode() {
        throw new MethodNotImplementedException();
    }

    public void parseSigJs() {
        throw new MethodNotImplementedException();
    }

    public void decryptSignature() {
        throw new MethodNotImplementedException();
    }

    public void markWatched() {
        throw new MethodNotImplementedException();
    }

    public void extractUrls(String webPage) {
        throw new MethodNotImplementedException();
    }

    public void extractUrl(String webPage) {
        throw new MethodNotImplementedException();
    }

    public void extractId(String url) {
        throw new MethodNotImplementedException();
    }

    public List<Object> extractChaptersFromJson(JSONObject data, String videoId, int duration) {
        log.error("Not implemented!");
        return Collections.emptyList();
    }

    public JSONObject extractYtInitialVariable(String webPage, String regex, String videoId, String name) {
        return super.parseJson(
                super.searchRegex(
                        Arrays.asList(format("%s\\s*%s", regex, _YT_INITIAL_BOUNDARY_RE), regex),
                        webPage,
                        name
                ),
                videoId
        );
    }
}

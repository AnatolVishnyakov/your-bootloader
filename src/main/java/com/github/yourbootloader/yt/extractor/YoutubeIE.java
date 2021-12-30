package com.github.yourbootloader.yt.extractor;

import com.github.yourbootloader.yt.exception.MethodNotImplementedException;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.lang.String.format;

@Slf4j
@Service
public class YoutubeIE extends YoutubeBaseInfoExtractor {

    private static String IE_DESC = "YouTube.com";
    public static List<String> _INVIDIOUS_SITES = Arrays.asList(
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
    public static String _VALID_URL = ("(?x)^" +
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
            "(?<id>[0-9A-Za-z_-]{11})" +                                                          // here is it! the YouTube video ID
            "(.+)?" +                                                                         // if we found the ID, everything can follow
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

    // TODO требует обязательной реализации
    public Info realExtract(String _url) {
        Map<Object, Object> smuggledData = Utils.unsmuggleUrl(_url).getTwo();
        String url = Utils.unsmuggleUrl(_url).getOne();
        String videoId = this.matchId(url);
        String baseUrl = this.httpScheme() + ":" + "//www.youtube.com/";
        String webPageUrl = baseUrl + "watch?v=" + videoId;
        String webpage = this.downloadWebpage(webPageUrl + "&bpctr=9999999999&has_verified=1", videoId, 3);

        JSONObject playerResponse = null;
        if (webpage != null && !webpage.isEmpty()) {
            playerResponse = this.extractYtInitialVariable(webpage, _YT_INITIAL_PLAYER_RESPONSE_RE, videoId, "initial player response");
        }

        if (playerResponse == null) {
            throw new MethodNotImplementedException("Player response");
        }

        JSONObject playabilityStatus = playerResponse.getJSONObject("playabilityStatus");
        if (playabilityStatus.has("reason") &&
                playabilityStatus.getString("reason").equals("Sign in to confirm your age")) {
            throw new MethodNotImplementedException("Player response playabilityStatus");
        }

        if (isTrailerVideoId(playabilityStatus)) {
            throw new MethodNotImplementedException("Trailer video id handler not implements!");
        }

        JSONObject videoDetails = playerResponse.getJSONObject("videoDetails");
        JSONObject microformat = playerResponse.getJSONObject("microformat").getJSONObject("playerMicroformatRenderer");
        String videoTitle = videoDetails.getString("title");
        String videoDescription = videoDetails.getString("shortDescription");

        // TODO force_singlefeed

        List<Map<String, Object>> formats = new ArrayList<>();
        List<Integer> itags = new ArrayList<>();
        Map<Integer, String> itagQualities = new HashMap<>();
        Object player_url = null;
        Function<String, Integer> q = key -> Arrays.asList("tiny", "small", "medium", "large", "hd720", "hd1080", "hd1440", "hd2160", "hd2880", "highres").indexOf(key);
        JSONObject streamingData = playerResponse.getJSONObject("streamingData");
        JSONArray streamingFormats = streamingData.getJSONArray("formats");
        JSONArray adaptiveFormats = streamingData.getJSONArray("adaptiveFormats");

        for (int i = 0; i < adaptiveFormats.length(); i++) {
            streamingFormats.put(adaptiveFormats.get(i));
        }

        for (int i = 0; i < streamingFormats.length(); i++) {
            JSONObject fmt = streamingFormats.getJSONObject(i);
            if (fmt.has("targetDurationSec") || fmt.has("drmFamilies")) {
                continue;
            }

            Integer itag = null;
            String quality = null;
            if (fmt.has("itag") && fmt.has("quality")) {
                itag = fmt.getInt("itag");
                quality = fmt.getString("quality");
                itagQualities.put(itag, quality);
            }

            if (fmt.has("type") && fmt.getString("type").equals("FORMAT_STREAM_TYPE_OTF")) {
                continue;
            }

            String fmtUrl = fmt.getString("url");
            if (fmtUrl == null && !fmt.isEmpty()) {
                throw new MethodNotImplementedException("Not implement fmtUrl");
            }

            if (itag != null) {
                itags.add(itag);
            }

            float tbr = fmt.has("averageBitrate")
                    ? fmt.getFloat("averageBitrate")
                    : (fmt.has("bitrate") ? fmt.getFloat("bitrate") : 1000);

            Integer finalItag = itag;
            String finalQuality = quality;
            Map<String, Object> dct = new HashMap<String, Object>() {{
                put("asr", fmt.has("audioSampleRate") ? fmt.getInt("audioSampleRate") : null);
                put("filesize", fmt.has("contentLength") ? fmt.getInt("contentLength") : null);
                put("format_id", finalItag);
                put("format_note", fmt.has("qualityLabel") ? fmt.getString("qualityLabel") : finalQuality);
                put("fps", fmt.has("fps") ? fmt.getInt("fps") : null);
                put("height", fmt.has("height") ? fmt.get("height") : null);
                put("quality", q.apply(finalQuality));
                put("tbr", tbr);
                put("url", fmtUrl);
                put("width", fmt.has("width") ? fmt.get("width") : null);
            }};

            String mimeType = null;
            if (fmt.has("mimeType")) {
                mimeType = fmt.getString("mimeType");
                Pattern pattern = Pattern.compile("((?:[^/]+)/(?:[^;]+))(?:;\\s*codecs=\"([^\"]+)\")?");
                Matcher matcher = pattern.matcher(mimeType);
                if (matcher.find()) {
                    dct.put("ext", mimeType2Ext(matcher.group(1)));
                    dct.putAll(parseCodecs(matcher.group(2)));
                }
            }

            boolean noAudio = dct.get("acodec") == null;
            boolean noVideo = dct.get("vcodec") == null;

            if (noAudio) {
                dct.put("vbr", tbr);
            }
            if (noVideo) {
                dct.put("abr", tbr);
            }
            if (noAudio || noVideo) {
                dct.put("downloader_options", new HashMap<String, Object>() {{
                    put("http_chunk_size", 10485760);
                }});
                if (dct.containsKey("ext")) {
                    dct.put("container", dct.get("ext") + "_dash");
                }
            }

            formats.add(dct);
        }
        return null;
    }

    private Map<String, Object> parseCodecs(String codecs) {
        if (codecs == null || codecs.isEmpty()) {
            return Collections.emptyMap();
        }

        List<String> splitCodecs = Arrays.stream(codecs.split(",")).map(String::trim).collect(Collectors.toList());
        String vcodec = null;
        String acodec = null;

        for (String fullCodec : splitCodecs) {
            String codec = fullCodec.split("\\.")[0];
            if (Arrays.asList("avc1", "avc2", "avc3", "avc4", "vp9", "vp8", "hev1", "hev2", "h263", "h264", "mp4v", "hvc1", "av01", "theora").contains(codec)) {
                if (vcodec == null) {
                    vcodec = fullCodec;
                }
            } else if (Arrays.asList("mp4a", "opus", "vorbis", "mp3", "aac", "ac-3", "ec-3", "eac3", "dtsc", "dtse", "dtsh", "dtsl").contains(codec)) {
                if (acodec == null) {
                    acodec = fullCodec;
                }
            } else {
                log.warn("WARNING: Unknown codec {}", fullCodec);
            }
        }

        if (acodec != null && vcodec != null) {
            if (splitCodecs.size() == 2) {
                return new HashMap<String, Object>() {{
                    put("vcodec", splitCodecs.get(0));
                    put("acodec", splitCodecs.get(1));
                }};
            }
        }

        return new HashMap<String, Object>() {{
            put("vcodec", null);
            put("acodec", null);
        }};
    }

    private boolean isTrailerVideoId(JSONObject playabilityStatus) {
        return playabilityStatus.has("errorScreen")
                && playabilityStatus.getJSONObject("errorScreen").has("playerLegacyDesktopYpcTrailerRenderer")
                && playabilityStatus.getJSONObject("errorScreen").getJSONObject("playerLegacyDesktopYpcTrailerRenderer")
                .has("trailerVideoId");
    }

    private String mimeType2Ext(String mt) {
        if (mt == null || mt.isEmpty()) {
            return null;
        }

        String ext = new HashMap<String, String>() {{
            put("audio/mp4", "m4a");
            put("audio/mpeg", "mp3");
        }}.get(mt);

        if (ext != null) {
            return ext;
        }

        String res = mt.split("/", 2)[1];
        res = res.split(";")[0].trim().toLowerCase();

        return new HashMap<String, String>() {{
            put("3gpp", "3gp");
            put("smptett+xml", "tt");
            put("ttaf+xml", "dfxp");
            put("ttml+xml", "ttml");
            put("x-flv", "flv");
            put("x-mp4-fragmented", "mp4");
            put("x-ms-sami", "sami");
            put("x-ms-wmv", "wmv");
            put("mpegurl", "m3u8");
            put("x-mpegurl", "m3u8");
            put("vnd.apple.mpegurl", "m3u8");
            put("dash+xml", "mpd");
            put("f4m+xml", "f4m");
            put("hds+xml", "f4m");
            put("vnd.ms-sstr+xml", "ism");
            put("quicktime", "mov");
            put("mp2t", "ts");
            put("x-wav", "wav");
        }}.getOrDefault(res, res);
    }
}

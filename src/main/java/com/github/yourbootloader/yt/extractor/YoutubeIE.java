package com.github.yourbootloader.yt.extractor;

import com.github.yourbootloader.yt.download.TempFileGenerator;
import com.github.yourbootloader.yt.exception.MethodNotImplementedException;
import com.github.yourbootloader.yt.extractor.dto.Pair;
import com.github.yourbootloader.yt.extractor.interpreter.JSInterpreter;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.lang.String.format;

@Slf4j
@Service
public class YoutubeIE extends YoutubeBaseInfoExtractor {

    private static final String IE_DESC = "YouTube.com";
    public static String _VALID_URL = "(?x)^" +
            "(" +
            "    (?:https?://|//)" +                                                              // http(s):// or protocol-independent URL
            "    (?:(?:(?:(?:\\w+\\.)?[yY][oO][uU][tT][uU][bB][eE](?:-nocookie|kids)?\\.com|" +
            "       (?:www\\.)?deturl\\.com/www\\.youtube\\.com|" +
            "       (?:www\\.)?pwnyoutube\\.com|" +
            "       (?:www\\.)?hooktube\\.com|" +
            "       (?:www\\.)?yourepeat\\.com|" +
            "       tube\\.majestyc\\.net|" +
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
            "       zwearz\\.com/watch" +                                                        // or zwearz.com/watch/xxxx
            "    )/" +
            "    |(?:www\\.)?cleanvideosearch\\.com/media/action/yt/watch\\?videoId=" +
            "    )" +
            ")?" +                                                                                // all until now is optional -> you can pass the naked ID
            "(?<id>[0-9A-Za-z_-]{11})" +                                                          // here is it! the YouTube video ID
            "(.+)?" +                                                                         // if we found the ID, everything can follow
            "(\\?(1).+)?" +                                                                         // if we found the ID, everything can follow
            "$";
    private static final List<Pattern> _PLAYER_INFO_RE = Arrays.asList(
            Pattern.compile("/s/player/(?<id>[a-zA-Z0-9_-]{8,})/player"),
            Pattern.compile("/(?<id>[a-zA-Z0-9_-]{8,})/player(?:_ias\\.vflset(?:/[a-zA-Z]{2,3}_[a-zA-Z]{2,3})?|-plasma-ias-(?:phone|tablet)-[a-z]{2}_[A-Z]{2}\\.vflset)/base\\.js$"),
            Pattern.compile("\b(?<id>vfl[a-zA-Z0-9_-]+)\b.*?\\.js$")
    );
    private static final List<String> _SUBTITLE_FORMATS = Arrays.asList("srv1", "srv2", "srv3", "ttml", "vtt");
    private static final boolean _GEO_BYPASS = false;
    private static final String IE_NAME = "youtube";
    private final Map<Object, Object> codeCache;
    private final Map<Pair<String, String>, Function<String, String>> playerCache;
    private final TempFileGenerator tempFileGenerator;

    @Autowired
    protected YoutubeIE(YoutubeDLService youtubeDLService, TempFileGenerator tempFileGenerator) {
        super(youtubeDLService);
        this.tempFileGenerator = tempFileGenerator;
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

    public String signatureCacheId(String exampleSig) {
        StringBuilder result = new StringBuilder();
        for (String part : exampleSig.split("\\.")) {
            result.append(part.length());
        }
        return result.toString();
    }

    public String extractPlayerInfo(String playerUrl) {
        for (Pattern pattern : _PLAYER_INFO_RE) {
            Matcher matcher = pattern.matcher(playerUrl);
            if (matcher.find()) {
                return matcher.group("id");
            }
        }
        throw new RuntimeException("Cannot identify player " + playerUrl);
    }

    public Function<String, String> extractSignatureFunction(String videoId, String playerUrl, String exampleSig) {
        String playerId = this.extractPlayerInfo(playerUrl);

        String funcId = format("js_%s_%s", playerId, this.signatureCacheId(exampleSig));

        Optional<File> cacheSpec = this.getCacheFn("youtube-sigfuncs", funcId, "json");
        if (cacheSpec.isPresent()) {
            // TODO
            throw new MethodNotImplementedException("extractSignatureFunction doesn't implemented!");
        }

        if (!codeCache.containsKey(playerId)) {
            codeCache.put(playerId, this.downloadWebpage(playerUrl, videoId, 3));
        }

        String code = ((String) codeCache.get(playerId));
        Function<String, String> res = this.parseSigJs(code);

        String testString = IntStream.range(0, exampleSig.length())
                .mapToObj(i -> ((char) i))
                .map(String::valueOf)
                .collect(Collectors.joining(""));

        String cacheRes = res.apply(testString);
        // TODO cache
        return res;
    }

    private Optional<File> getCacheFn(String section, String key, String dtype) {
        return Optional.ofNullable(tempFileGenerator.get(section, format("%s.%s", key, dtype)));
    }

    public void printSigCode() {
        throw new MethodNotImplementedException();
    }

    public Function<String, String> parseSigJs(String jscode) {
        List<Pattern> patterns = Arrays.asList(
                Pattern.compile("\b[cs]\\s*&&\\s*[adf]\\.set\\([^,]+\\s*,\\s*encodeURIComponent\\s*\\(\\s*(?<sig>[a-zA-Z0-9$]+)\\("),
                Pattern.compile("\b[a-zA-Z0-9]+\\s*&&\\s*[a-zA-Z0-9]+\\.set\\([^,]+\\s*,\\s*encodeURIComponent\\s*\\(\\s*(?<sig>[a-zA-Z0-9$]+)\\("),
                Pattern.compile("\bm=(?<sig>[a-zA-Z0-9$]{2})\\(decodeURIComponent\\(h\\.s\\)\\)"),
                Pattern.compile("\bc&&\\(c=(?<sig>[a-zA-Z0-9$]{2})\\(decodeURIComponent\\(c\\)\\)"),
                Pattern.compile("(?:\b|[^a-zA-Z0-9$])(?<sig>[a-zA-Z0-9$]{2})\\s*=\\s*function\\(\\s*a\\s*\\)\\s*\\{\\s*a\\s*=\\s*a\\.split\\(\\s*\"\"\\s*\\);[a-zA-Z0-9$]{2}\\.[a-zA-Z0-9$]{2}\\(a,\\d+\\)"),
                Pattern.compile("(?:\b|[^a-zA-Z0-9$])(?<sig>[a-zA-Z0-9$]{2})\\s*=\\s*function\\(\\s*a\\s*\\)\\s*\\{\\s*a\\s*=\\s*a\\.split\\(\\s*\"\"\\s*\\)"),
                Pattern.compile("(?<sig>[a-zA-Z0-9$]+)\\s*=\\s*function\\(\\s*a\\s*\\)\\s*\\{\\s*a\\s*=\\s*a\\.split\\(\\s*\"\"\\s*\\)"),
                Pattern.compile("([\"'])signature\\1\\s*,\\s*(?<sig>[a-zA-Z0-9$]+)\\("),
                Pattern.compile("\\.sig\\|\\|(?<sig>[a-zA-Z0-9$]+)\\("),
                Pattern.compile("yt\\.akamaized\\.net/\\)\\s*\\|\\|\\s*.*?\\s*[cs]\\s*&&\\s*[adf]\\.set\\([^,]+\\s*,\\s*(?:encodeURIComponent\\s*\\()?\\s*(?<sig>[a-zA-Z0-9$]+)\\("),
                Pattern.compile("\b[cs]\\s*&&\\s*[adf]\\.set\\([^,]+\\s*,\\s*(?<sig>[a-zA-Z0-9$]+)\\("),
                Pattern.compile("\b[a-zA-Z0-9]+\\s*&&\\s*[a-zA-Z0-9]+\\.set\\([^,]+\\s*,\\s*(?<sig>[a-zA-Z0-9$]+)\\("),
                Pattern.compile("\bc\\s*&&\\s*a\\.set\\([^,]+\\s*,\\s*\\([^)]*\\)\\s*\\(\\s*(?<sig>[a-zA-Z0-9$]+)\\("),
                Pattern.compile("\bc\\s*&&\\s*[a-zA-Z0-9]+\\.set\\([^,]+\\s*,\\s*\\([^)]*\\)\\s*\\(\\s*(?<sig>[a-zA-Z0-9$]+)\\("),
                Pattern.compile("\bc\\s*&&\\s*[a-zA-Z0-9]+\\.set\\([^,]+\\s*,\\s*\\([^)]*\\)\\s*\\(\\s*(?<sig>[a-zA-Z0-9$]+)\\(")
        );

        String funcName = this.searchRegex(patterns, jscode, "Initial JS player signature function name", "sig");

        JSInterpreter jsi = new JSInterpreter(jscode);
        return jsi.extractFunction(funcName);
    }

    public String decryptSignature(String s, String videoId, String playerUrl) {
        if (playerUrl == null) {
            throw new RuntimeException("Cannot decrypt signature without player_url");
        }

        if (playerUrl.startsWith("//")) {
            playerUrl = "https:" + playerUrl;
        } else if (!Pattern.compile("https?://").matcher(playerUrl).matches()) {
            playerUrl = "https://www.youtube.com" + playerUrl;
        }

        Pair<String, String> playerId = new Pair<String, String>(
                playerUrl,
                this.signatureCacheId(s)
        );
        if (!playerCache.containsKey(playerId)) {
            Function<String, String> func = this.extractSignatureFunction(videoId, playerUrl, s);
            this.playerCache.put(playerId, func);
        }

        Function<String, String> func = this.playerCache.get(playerId);
        return func.apply(s);
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

    @Override
    public YtVideoInfo realExtract(String _url) {
        String url = Utils.unsmuggleUrl(_url).getOne();
        String videoId = this.matchId(url);
        String baseUrl = this.httpScheme() + ":" + "//www.youtube.com/";
        String webPageUrl = baseUrl + "watch?v=" + videoId;
        String webpage = this.downloadWebpage(webPageUrl + "&bpctr=9999999999&has_verified=1", videoId, 3);
        return realExtract(_url, webpage);
    }

    public YtVideoInfo realExtract(String _url, String webpage) {
        Map<Object, Object> smuggledData = Utils.unsmuggleUrl(_url).getTwo();
        String url = Utils.unsmuggleUrl(_url).getOne();
        String videoId = this.matchId(url);
        String baseUrl = this.httpScheme() + ":" + "//www.youtube.com/";
        String webPageUrl = baseUrl + "watch?v=" + videoId;

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
        String playerUrl = null;
        Function<String, Integer> q = key -> Arrays.asList("tiny", "small", "medium", "large", "hd720", "hd1080", "hd1440", "hd2160", "hd2880", "highres").indexOf(key);
        JSONObject streamingData = Optional.of(playerResponse).map(sd -> sd.optJSONObject("streamingData")).orElse(new JSONObject());
        JSONArray streamingFormats = Optional.of(streamingData).map(sf -> sf.optJSONArray("formats")).orElse(new JSONArray());
        JSONArray adaptiveFormats = Optional.of(streamingData).map(sd -> sd.optJSONArray("adaptiveFormats")).orElse(new JSONArray());

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

            AtomicReference<String> fmtUrl = new AtomicReference<>(fmt.optString("url"));
            if (fmtUrl.get() == null || fmtUrl.get().isEmpty()) {
                Map<String, String> sc = Utils.parseQs(fmt.optString("signatureCipher"));
                fmtUrl.set(sc.get("url"));
                String encryptedSig = sc.get("s");
                if (sc.isEmpty() || fmtUrl.get() == null || encryptedSig == null) {
                    continue;
                }

                if (playerUrl == null) {
                    if (webpage == null) {
                        continue;
                    }

                    playerUrl = searchRegex(
                            Arrays.asList("\"(?:PLAYER_JS_URL|jsUrl)\"\\s*:\\s*\"([^\"]+)\""),
                            webpage,
                            "player URL"
                    );
                }

                if (playerUrl == null) {
                    continue;
                }

                String signature = decryptSignature(sc.get("s"), videoId, playerUrl);
                fmtUrl.set(fmtUrl.get() + "&" + sc.get("sp") + "=" + signature);
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
                put("filesize", fmt.has("contentLength") ? fmt.getLong("contentLength") : null);
                put("format_id", finalItag);
                put("format_note", fmt.has("qualityLabel") ? fmt.getString("qualityLabel") : finalQuality);
                put("fps", fmt.has("fps") ? fmt.getInt("fps") : null);
                put("height", fmt.has("height") ? fmt.get("height") : null);
                put("quality", q.apply(finalQuality));
                put("tbr", tbr);
                put("url", fmtUrl.get());
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

        if (streamingData.has("hlsManifestUrl")) {
            String hlsManifestUrl = streamingData.getString("hlsManifestUrl");
            throw new MethodNotImplementedException("Hls manifest url: " + hlsManifestUrl);
        }

        if (((boolean) downloader.getParams("youtube_include_dash_manifest"))) {
            if (streamingData.has("dashManifestUrl")) {
                String dashManifestUrl = streamingData.getString("dashManifestUrl");
                if (dashManifestUrl != null && !dashManifestUrl.isEmpty()) {
                    throw new MethodNotImplementedException("Manifest url not implemented!");
                }
            }
        }

        if (formats.isEmpty()) {
            throw new MethodNotImplementedException("Formats not found. Implements another source");
        }

        // TODO implements sort formats
        // TODO implements keywords

        List<Map<String, Object>> thumbnails = new ArrayList<>();

        for (JSONObject container : Arrays.asList(videoDetails, microformat)) {
            container.optJSONObject("thumbnail").optJSONArray("thumbnails").forEach(i -> {
                JSONObject thumbnail = (JSONObject) i;
                String thumbnailUrl = thumbnail.optString("url");
                if (thumbnailUrl == null || thumbnailUrl.isEmpty()) {
                    return;
                }
                thumbnails.add(
                        new HashMap<String, Object>() {{
                            put("height", thumbnail.optInt("height"));
                            put("url", thumbnailUrl);
                            put("width", thumbnail.optInt("width"));
                        }}
                );
            });
            if (!thumbnails.isEmpty()) {
                break;
            }
        }

        String category = Optional.ofNullable(microformat.optString("category"))
                .orElse(searchMeta("genre"));
        String channelId = Optional.ofNullable(videoDetails.optString("channelId"))
                .orElseGet(() -> Optional.ofNullable(microformat.optString("externalChannelId"))
                        .orElse(searchMeta("channelId")));
        int duration = Optional.ofNullable(videoDetails.optInt("lengthSeconds"))
                .orElseGet(() -> Optional.ofNullable(microformat.optInt("lengthSeconds"))
                        .orElse(Utils.parseDuration(searchMeta("duration"))));
        boolean isLive = videoDetails.optBoolean("isLive");
        String ownerProfileUrl = microformat.optString("ownerProfileUrl");

//        Map<String, Object> info = new HashMap<String, Object>() {{
//            put("id", videoId);
//            put("title", videoTitle);
//            put("formats", formats);
//            put("thumbnails", thumbnails);
//            put("description", videoDescription);
//            put("upload_date", microformat.optString("uploadDate"));
//            put("uploader", videoDetails.optString("author"));
//            put("uploader_id", null);
//            put("uploader_url", ownerProfileUrl);
//            put("channel_id", channelId);
//            put("channel_url", channelId != null && !channelId.isEmpty() ? "https://www.youtube.com/channel/" + channelId : null);
//            put("duration", duration);
//            put("view_count", Optional.ofNullable(videoDetails.optInt("viewCount"))
//                    .orElseGet(() -> microformat.optInt("viewCount")));
//            put("average_rating", videoDetails.optFloat("averageRating"));
//            put("age_limit", null);
//            put("webpage_url", webPageUrl);
//            put("categories", Collections.singletonList(category));
//            put("tags", null);
//            put("is_live", isLive);
//        }};
//
//        return info;

        return YtVideoInfo.builder()
                .id(videoId)
                .title(videoTitle)
                .formats(formats)
                .thumbnails(thumbnails)
                .description(videoDescription)
                .uploadDate(microformat.optString("uploadDate"))
                .uploader(videoDetails.optString("author"))
                .uploaderId(null)
                .uploaderUrl(ownerProfileUrl)
                .channelId(channelId)
                .channelUrl(channelId != null && !channelId.isEmpty() ? "https://www.youtube.com/channel/" + channelId : null)
                .duration(duration)
                .viewCount(Optional.ofNullable(videoDetails.optInt("viewCount"))
                        .orElseGet(() -> microformat.optInt("viewCount")))
                .averageRating(videoDetails.optFloat("averageRating"))
                .ageLimit(null)
                .webpageUrl(webPageUrl)
                .categories(Collections.singletonList(category))
                .tags(null)
                .isLive(isLive)
                .build();
    }

    private String searchMeta(String word) {
        log.warn("Not implement search meta");
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

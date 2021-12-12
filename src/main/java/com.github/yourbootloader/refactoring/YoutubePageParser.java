package com.github.yourbootloader.refactoring;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.String.format;

@Slf4j
public class YoutubePageParser {
    private static final String YT_INITIAL_PLAYER_RESPONSE_RE = "ytInitialPlayerResponse\\s*=\\s*(\\{.+?})\\s*;";
    private static final String YT_INITIAL_BOUNDARY_RE = "(?:var\\s+meta|</script|\n)";
    private final YoutubeUrl youtubeUrl;
    private final DownloadWebPage downloadWebPage;

    public YoutubePageParser(String url) {
        this.youtubeUrl = new YoutubeUrl(url);
        this.downloadWebPage = new DownloadWebPage(youtubeUrl);
    }

    @SneakyThrows
    public void parse() {
        String content = downloadWebPage.download();
        PlayerResponse playerResponse = null;
        if (content != null) {
            playerResponse = extractYtInitialVariable(content);
        }

        if (playerResponse == null) {
            // TODO реализовать вызов по API
            throw new RuntimeException("Player not found!");
        }

        if (playerResponse.getReason().equals("Sign in to confirm your age")) {
            // TODO реализовать подтверждение возраста
            throw new RuntimeException("Refetching age-gated info webpage");
        }

        // TODO реализовать пропуск трейлера,
        //  сбор информации о видео
        JSONObject videoDetails = playerResponse.getVideoDetails();
        JSONObject microformat = playerResponse.getMicroformat();
        String videoTitle = videoDetails.getString("title");
        String videoDescription = videoDetails.getString("shortDescription");
        JSONObject streamingData = playerResponse.getStreamingData();
        JSONArray streamingFormats = streamingData.getJSONArray("formats");
        JSONArray adaptiveFormats = streamingData.getJSONArray("adaptiveFormats");

        for (int i = 0; i < adaptiveFormats.length(); i++) {
            streamingFormats.put(adaptiveFormats.get(i));
        }

        List<Map<String, Object>> formats = new ArrayList<>();
        ArrayList<String> q = new ArrayList<>(Arrays.asList("tiny", "small", "medium", "large", "hd720", "hd1080", "hd1440", "hd2160", "hd2880", "highres"));
        Set<String> itags = new HashSet<>();
        Map<String, String> itagQualities = new HashMap<>();
        for (int i = 0; i < streamingFormats.length(); i++) {
            JSONObject fmt = streamingFormats.getJSONObject(i);
            if (fmt.has("targetDurationSec") || fmt.has("drmFamilies")) {
                continue;
            }

            String itag = null;
            String quality = null;
            if (fmt.has("itag") && fmt.has("quality")) {
                itag = fmt.getString("itag");
                quality = fmt.getString("quality");
                itagQualities.put(itag, quality);
            }

            if (fmt.has("type") && fmt.getString("type").equals("FORMAT_STREAM_TYPE_OTF")) {
                continue;
            }

            String fmtUrl = null;
            if (fmt.has("url")) {
                fmtUrl = fmt.getString("url");
            } else {
                // TODO распарсить signatureCipher
                throw new RuntimeException("Fmt url not found.");
            }

            if (itag != null) {
                itags.add(itag);
            }

            //             tbr = float_or_none(
            //                fmt.get("averageBitrate") or fmt.get("bitrate"), 1000)
            double tbr = 1_000d;
            if (fmt.has("averageBitrate")) {
                tbr = fmt.getDouble("averageBitrate");
            } else if (fmt.has("bitrate")) {
                tbr = fmt.getDouble("bitrate");
            }

            Map<String, Object> dct = new HashMap<String, Object>();
            dct.put("asr", fmt.optInt("audioSampleRate"));
            dct.put("filesize", fmt.optInt("contentLength"));
            dct.put("format_id", itag);
            dct.put("format_note", fmt.optString("qualityLabel", quality));
            dct.put("fps", fmt.optInt("fps"));
            dct.put("height", fmt.optInt("height"));
            dct.put("quality", q.indexOf(quality));
            dct.put("tbr", tbr);
            dct.put("url", fmtUrl);
            dct.put("width", fmt.optString("width"));

            if (fmt.has("mimeType")) {
                Pattern pattern = Pattern.compile("((?:[^/]+)/(?:[^;]+))(?:;\\s*codecs=\"([^\"]+)\")?");
                Matcher matcher = pattern.matcher(fmt.getString("mimeType"));
                if (matcher.find()) {
                    dct.put("ext", mimeType2Ext(matcher.group(1)));
                    // TODO распарсить информацию о кодеке
                }
            }

            // TODO реализовать обработку acoded/vcodec
            dct.put("acodec", null);
            dct.put("vcodec", null);

            formats.add(dct);
        }

        // TODO обработка hlsManifestUrl
        // TODO обработка youtube_include_dash_manifest
        if (formats.isEmpty()) {
            throw new RuntimeException("The uploader has not made this video available in your country.");
        }

        // TODO сортировка форматов
        // TODO обработка keywords
        // TODO обработка thumbnails

        System.out.println();
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

    private PlayerResponse extractYtInitialVariable(String content) {
        Pattern[] patterns = {
                Pattern.compile(format("%s\\s*%s", YT_INITIAL_PLAYER_RESPONSE_RE, YT_INITIAL_BOUNDARY_RE)),
                Pattern.compile(YT_INITIAL_PLAYER_RESPONSE_RE)
        };

        Matcher matcher = null;
        for (Pattern pattern : patterns) {
            matcher = pattern.matcher(content);
            if (matcher.find()) {
                return new PlayerResponse(matcher.group(1), youtubeUrl.getVideoId());
            }
        }

        throw new RuntimeException(format("Unable to extract %s", "initial player response"));
    }

    public static void main(String[] args) {
        String url = "https://www.youtube.com/watch?v=nui3hXzcbK0";
        YoutubePageParser youtubePageParser = new YoutubePageParser(url);
        youtubePageParser.parse();
    }
}
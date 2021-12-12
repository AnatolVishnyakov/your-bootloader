package com.github.yourbootloader.refactoring;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;

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
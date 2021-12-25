package com.github.yourbootloader.yt.extractor;

import lombok.SneakyThrows;
import org.json.JSONObject;

public class PlayerResponse {
    private final String playerTag;
    private final String videoId;
    private final JSONObject json;

    @SneakyThrows
    public PlayerResponse(String playerTag, String videoId) {
        this.playerTag = playerTag;
        this.videoId = videoId;
        this.json = new JSONObject(playerTag);
    }

    @SneakyThrows
    public JSONObject getPlayabilityStatus() {
        return json.getJSONObject("playabilityStatus");
    }

    @SneakyThrows
    public String getReason() {
        String reason = "reason";
        if (getPlayabilityStatus().has(reason)) {
            return getPlayabilityStatus().getString(reason);
        }
        return "<Reason not found>";
    }

    @SneakyThrows
    public JSONObject getVideoDetails() {
        return json.getJSONObject("videoDetails");
    }

    @SneakyThrows
    public JSONObject getMicroformat() {
        if (json.has("microformat")) {
            JSONObject microformat = json.getJSONObject("microformat");
            if (microformat.has("playerMicroformatRenderer")) {
                return microformat.getJSONObject("playerMicroformatRenderer");
            }
        }
        return null;
    }

    @SneakyThrows
    public JSONObject getStreamingData() {
        return json.getJSONObject("streamingData");
    }
}

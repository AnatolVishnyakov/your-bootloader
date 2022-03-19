package com.github.yourbootloader.yt.extractor;

import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Builder
@Getter
public class YtVideoInfo {

    String id;

    String title;

    List<Map<String, Object>> formats;

    List<?> thumbnails;

    String description;

    String uploadDate;

    String uploader;

    String uploaderId;

    String uploaderUrl;

    String channelId;

    String channelUrl;

    Integer duration;

    Integer viewCount;

    Float averageRating;

    Integer ageLimit;

    String webpageUrl;

    List<?> categories;

    List<?> tags;

    boolean isLive;

    int likeCount;

    String channel;

    String track;

    String artist;

    String album;

    String creator;

    String altTitle;

}

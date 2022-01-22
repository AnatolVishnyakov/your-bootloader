package com.github.yourbootloader.bot.dto;

import lombok.Value;

@Value
public class VideoInfoDto {

    Integer formatId;

    String title;

    Long filesize;

    String url;

}

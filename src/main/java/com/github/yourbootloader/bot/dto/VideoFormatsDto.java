package com.github.yourbootloader.bot.dto;

import lombok.Value;

import java.util.List;
import java.util.Map;

@Value
public class VideoFormatsDto {

    String title;

    List<Map<String, Object>> formats;

}

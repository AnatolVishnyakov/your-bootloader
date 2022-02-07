package com.github.yourbootloader.bot.command.cache;

import com.github.yourbootloader.bot.dto.VideoInfoDto;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class CommandCache {

    private final Map<Long, List<VideoInfoDto>> cache;

    public CommandCache() {
        this.cache = new HashMap<>();
    }

    public void save(Long chatId, List<VideoInfoDto> videoInfo) {
        cache.put(chatId, videoInfo);
    }

    public List<VideoInfoDto> get(Long chatId) {
        return cache.get(chatId);
    }

    public void delete(Long chatId) {
        cache.remove(chatId);
    }
}

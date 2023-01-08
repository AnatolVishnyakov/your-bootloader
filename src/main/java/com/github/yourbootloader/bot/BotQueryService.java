package com.github.yourbootloader.bot;

import com.github.yourbootloader.bot.dto.VideoFormatsDto;
import com.github.yourbootloader.yt.extractor.YoutubeIE;
import com.github.yourbootloader.yt.extractor.YtVideoInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BotQueryService {

    private final YoutubeIE youtubeIE;

    public YtVideoInfo getVideoInfo(String url) {
        return youtubeIE.realExtract(url);
    }

    public VideoFormatsDto getVideoFormats(String url) {
        YtVideoInfo videoInfo = youtubeIE.realExtract(url);
        List<Map<String, Object>> formats = videoInfo.getFormats();
        String title = videoInfo.getTitle();
        return new VideoFormatsDto(title, formats);
    }
}

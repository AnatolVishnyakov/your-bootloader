package com.github.yourbootloader.bot;

import com.github.yourbootloader.yt.extractor.YoutubeIE;
import com.github.yourbootloader.yt.extractor.YtVideoInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BotQueryService {

    private final YoutubeIE youtubeIE;

    public YtVideoInfo getVideoInfo(String url) {
        return youtubeIE.realExtract(url);
    }
}

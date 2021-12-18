package com.github.yourbootloader.bot;

import com.github.yourbootloader.bot.event.ProgressIndicatorEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.util.unit.DataSize;

@Slf4j
@Service
public class BotCommandService {
    @EventListener
    public void onProgressIndicatorEvent(ProgressIndicatorEvent event) {
        long fileSize = event.getFileSize();
        int receivedLength = event.getBlockSize();

        if (fileSize < 1_024) {
            log.info("{} B ({}) block_size: {}", DataSize.ofBytes(fileSize), DataSize.ofBytes(fileSize), receivedLength);
        } else if (fileSize < 1_048_576) {
            log.info("{} Kb ({}) block_size: {}", DataSize.ofBytes(fileSize).toKilobytes(), DataSize.ofBytes(fileSize), receivedLength);
        } else {
            log.info("{} Mb ({}) block_size: {}", DataSize.ofBytes(fileSize).toMegabytes(), DataSize.ofBytes(fileSize), receivedLength);
        }
    }
}

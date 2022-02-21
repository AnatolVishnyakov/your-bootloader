package com.github.yourbootloader.bot;

import com.github.yourbootloader.bot.event.ProgressIndicatorEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.util.unit.DataSize;

@Slf4j
@Profile("!prod")
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BotManagerDev {
//    @EventListener
    public void onProgressIndicatorEventDebug(ProgressIndicatorEvent event) {
        log.info("Filesize: {} Mb ({} Kb)",
                DataSize.ofBytes(event.getFileSize()).toMegabytes(),
                DataSize.ofBytes(event.getFileSize()).toKilobytes()
        );
    }
}

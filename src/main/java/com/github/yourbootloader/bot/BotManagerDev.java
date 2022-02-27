package com.github.yourbootloader.bot;

import com.github.yourbootloader.bot.event.ProgressIndicatorEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.util.unit.DataSize;

import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Profile("!prod")
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BotManagerDev {

    private final AtomicLong prevByteReceived = new AtomicLong();

    @EventListener
    public void onProgressIndicatorEventDebug(ProgressIndicatorEvent event) {
        long receivedKBytes = DataSize.ofBytes(event.getFileSize()).toKilobytes();
        long byteReceived = Math.abs(prevByteReceived.get() - receivedKBytes);
        log.info("Filesize: {} Kb (speed - {} Kb)",
                DataSize.ofBytes(event.getFileSize()).toKilobytes(), byteReceived);
        prevByteReceived.set(receivedKBytes);
    }
}

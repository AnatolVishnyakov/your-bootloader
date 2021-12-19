package com.github.yourbootloader.bot;

import com.github.yourbootloader.bot.event.ProgressIndicatorEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.util.unit.DataSize;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BotCommandService {

    private final Bot bot;

    @EventListener
    public void onProgressIndicatorEventLogging(ProgressIndicatorEvent event) {
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

    @EventListener
    public void onProgressIndicatorEvent(ProgressIndicatorEvent event) {
        if (bot.getChatId() != null) {
            DataSize dataSize = DataSize.ofBytes(event.getFileSize());
            long downloadedContent = dataSize.toMegabytes() == 0
                    ? dataSize.toKilobytes()
                    : dataSize.toMegabytes();
            SendMessage message = new SendMessage(String.valueOf(bot.getChatId()), "Скачано " + downloadedContent);
            message.setChatId(bot.getChatId().toString());
            try {
                bot.execute(message);
            } catch (TelegramApiException e) {
                log.error("Возникла непредвиденная ошибка", e);
            }
        }
    }
}

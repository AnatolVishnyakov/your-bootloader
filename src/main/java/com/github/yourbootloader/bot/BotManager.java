package com.github.yourbootloader.bot;

import com.github.yourbootloader.bot.event.ProgressIndicatorEvent;
import com.github.yourbootloader.bot.event.StartDownloadEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.util.unit.DataSize;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BotManager {

    private final Bot bot;
    private static final Map<Chat, Map<String, Object>> cache = new HashMap<>();

    @EventListener
    public void onProgressIndicatorEventLogging(ProgressIndicatorEvent event) {
        long fileSize = event.getFileSize();
        int receivedLength = event.getBlockSize();
        DataSize contentSize = event.getContentSize();

        if (fileSize < 1_024) {
            log.info("Content-Length: {} Mb <---> {} B ({}) block_size: {}", contentSize.toMegabytes(), DataSize.ofBytes(fileSize), DataSize.ofBytes(fileSize), receivedLength);
        } else if (fileSize < 1_048_576) {
            log.info("Content-Length: {} Mb <---> {} Kb ({}) block_size: {}", contentSize.toMegabytes(), DataSize.ofBytes(fileSize).toKilobytes(), DataSize.ofBytes(fileSize), receivedLength);
        } else {
            log.info("Content-Length: {} Mb <---> {} Mb ({}) block_size: {}", contentSize.toMegabytes(), DataSize.ofBytes(fileSize).toMegabytes(), DataSize.ofBytes(fileSize), receivedLength);
        }
    }

    @EventListener
    public void onStartDownloadProcess(StartDownloadEvent event) {
        Long chatId = event.getChat().getId();
        SendMessage message = new SendMessage(String.valueOf(chatId), "Скачивание начинается...");

        try {
            Message msg = bot.execute(message);
            cache.put(event.getChat(), new HashMap<String, Object>() {{
                put("messageId", msg.getMessageId());
                put("chatId", chatId);
            }});
        } catch (TelegramApiException e) {
            log.error("Возникла непредвиденная ошибка", e);
        }
    }

    @EventListener
    public void onProgressIndicatorEvent(ProgressIndicatorEvent event) {
        Map<String, Object> data = cache.get(event.getChat());
        Long chatId = ((Long) data.get("chatId"));
        Integer messageId = (Integer) data.get("messageId");
        DataSize dataSize = DataSize.ofBytes(event.getFileSize());
        long downloadedContent = dataSize.toMegabytes() == 0
                ? dataSize.toKilobytes()
                : dataSize.toMegabytes();

        EditMessageText message = new EditMessageText("Скачано: " + downloadedContent);
        message.setChatId(chatId.toString());
        message.setMessageId(messageId);
        message.setParseMode(ParseMode.HTML);
        message.disableWebPagePreview();
        message.setReplyMarkup(new InlineKeyboardMarkup());

        try {
            bot.execute(message);
        } catch (TelegramApiException e) {
            log.error("Возникла непредвиденная ошибка", e);
        }
    }
}

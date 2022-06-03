package com.github.yourbootloader.bot.command;

import com.github.yourbootloader.bot.Bot;
import com.github.yourbootloader.bot.BotCommandService;
import com.github.yourbootloader.bot.command.cache.CommandCache;
import com.github.yourbootloader.bot.dto.VideoInfoDto;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class YtDownloadCommand implements Command {

    private final BotCommandService botCommandService;
    private final CommandCache commandCache;
//    private final YtProducer ytProducer;

    @Override
    public boolean canHandle(Update message) {
        return message.hasCallbackQuery();
    }

    @Override
    public void handle(Bot bot, Update update) {
        log.info("Download: {}", update.getCallbackQuery().getData());
        Message message = update.getCallbackQuery().getMessage();
        Chat chat = message.getChat();
        JSONObject callbackData = new JSONObject(update.getCallbackQuery().getData());
        log.debug("Callback Json: {}", callbackData);
        List<VideoInfoDto> videosInfo = commandCache.get(chat.getId());
        int formatId = callbackData.optInt("format_id");
        long filesize = callbackData.optLong("filesize");
        List<VideoInfoDto> collect = videosInfo.stream()
                .filter(v -> v.getFormatId().equals(formatId))
                .collect(Collectors.toList());
        VideoInfoDto videoInfoDto = collect.get(0);
        String url = videoInfoDto.getUrl();
        String title = videoInfoDto.getTitle();
        if (collect.size() > 1) {
            log.warn("Formats greater than 1 {} / {}", title, formatId);
            sendNotification(bot, chat.getId(), "Найдено несколько форматов " + collect);
            return;
        }
        if (url.isEmpty()) {
            sendNotification(bot, chat.getId(), "Повторите отправку url!");
            return;
        }
//        ytProducer.produce(new YtMessage(chat, url, title, filesize));
        commandCache.delete(chat.getId());
        deleteMessage(bot, chat.getId().toString(), message.getMessageId());
        botCommandService.download(chat, url, title, filesize);
    }

    @SneakyThrows
    private void sendNotification(Bot bot, Long chatId, String text) {
        SendMessage message = new SendMessage(chatId.toString(), text);
        bot.execute(message);
    }

    @SneakyThrows
    private void deleteMessage(Bot bot, String chatId, Integer messageId) {
        DeleteMessage message = new DeleteMessage(chatId, messageId);
        bot.execute(message);
    }
}

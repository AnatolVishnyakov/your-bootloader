package com.github.yourbootloader.bot.command;

import com.github.yourbootloader.bot.Bot;
import com.github.yourbootloader.bot.BotCommandService;
import com.github.yourbootloader.bot.dto.VideoInfoDto;
import com.github.yourbootloader.bot.event.RemoveMessageEvent;
import com.github.yourbootloader.utils.UserContextHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
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
    private final ApplicationEventPublisher publisher;

    @Override
    public boolean canHandle(Update message) {
        return message.hasCallbackQuery();
    }

    @Override
    public void handle(Bot bot, Update update) {
        log.info("Download: {}", update.getCallbackQuery().getData());
        Message message = update.getCallbackQuery().getMessage();
        Chat chat = message.getChat();
        String callbackFormatId = update.getCallbackQuery().getData();
        List<VideoInfoDto> videosInfo = UserContextHolder.getContext().getVideoInfo();
        VideoInfoDto videoInfo = videosInfo.stream()
                .peek(v -> log.info("Format: {}", v.getFormatId()))
                .filter(v -> v.getFormatId().equals(Integer.parseInt(callbackFormatId)))
                .findAny()
                .orElseThrow(() -> {
                    sendNotification(bot, chat.getId(), "Not found format tag.");
                    return new RuntimeException();
                });
        int formatId = videoInfo.getFormatId();
        long filesize = videoInfo.getFilesize();
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
        publisher.publishEvent(new RemoveMessageEvent(chat.getId().toString(), message.getMessageId()));
        botCommandService.download(bot, chat, url, title, filesize);
    }
}

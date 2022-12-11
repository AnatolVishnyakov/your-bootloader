package com.github.yourbootloader.utils;

import com.github.yourbootloader.bot.dto.VideoInfoDto;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.telegram.telegrambots.meta.api.objects.Chat;

import java.util.List;

public class UserContextHolder {

    private static final ThreadLocal<UserContext> USER_CONTEXT = new ThreadLocal<>();

    public static void setContext(Chat chat, List<VideoInfoDto> videoInfo) {
        USER_CONTEXT.set(new UserContext(chat, videoInfo));
    }

    public static UserContext getContext() {
        return USER_CONTEXT.get();
    }

    @Getter
    @RequiredArgsConstructor
    public static class UserContext {

        private final Chat chat;

        private final List<VideoInfoDto> videoInfo;

    }
}

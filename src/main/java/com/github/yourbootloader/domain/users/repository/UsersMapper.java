package com.github.yourbootloader.domain.users.repository;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.User;

@Component
public class UsersMapper {
    public Users map(User telegramUser) {
        Users user = new Users();
        user.setId(telegramUser.getId());
        user.setFirstName(telegramUser.getFirstName());
        user.setLastName(telegramUser.getLastName());
        user.setUserName(telegramUser.getUserName());
        user.setCanJoinGroups(telegramUser.getCanJoinGroups());
        user.setIsBot(telegramUser.getIsBot());
        user.setCanReadAllGroupMessages(telegramUser.getCanReadAllGroupMessages());
        user.setLanguageCode(telegramUser.getLanguageCode());
        return user;
    }
}

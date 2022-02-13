package com.github.yourbootloader.domain.chat;

import com.github.yourbootloader.domain.chat.repository.Chat;
import com.github.yourbootloader.domain.chat.repository.ChatMapper;
import com.github.yourbootloader.domain.chat.repository.ChatRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
//@Transactional
@RequiredArgsConstructor
public class ChatCommandService {

//    private final ChatRepository chatRepository;
//    private final ChatMapper chatMapper;

//    public Chat findOrCreateChat(org.telegram.telegrambots.meta.api.objects.Chat chat) {
//        return chatRepository.findById(chat.getId())
//                .orElseGet(() -> chatRepository.save(chatMapper.map(chat)));
//    }
}

package com.github.yourbootloader.domain.message;

import com.github.yourbootloader.domain.message.model.MessageDto;
import com.github.yourbootloader.domain.message.repository.MessageMapper;
import com.github.yourbootloader.domain.message.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class MessageCommandService {

    private final MessageRepository messageRepository;
    private final MessageMapper messageMapper;

    public void save(MessageDto messageDto) {
        messageRepository.save(messageMapper.map(messageDto));
    }
}

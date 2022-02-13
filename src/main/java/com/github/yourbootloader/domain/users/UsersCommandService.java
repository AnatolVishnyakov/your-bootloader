package com.github.yourbootloader.domain.users;

import com.github.yourbootloader.domain.users.repository.UsersMapper;
import com.github.yourbootloader.domain.users.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
//@Transactional
@RequiredArgsConstructor
public class UsersCommandService {

//    private final UsersRepository usersRepository;
//    private final UsersMapper usersMapper;

//    public Users findOrCreate(User telegramUser) {
//        return usersRepository.findById(telegramUser.getId())
//                .orElseGet(() -> usersRepository.save(usersMapper.map(telegramUser)));
//    }

}

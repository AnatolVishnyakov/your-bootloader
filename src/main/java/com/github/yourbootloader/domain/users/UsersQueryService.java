package com.github.yourbootloader.domain.users;

import com.github.yourbootloader.domain.users.repository.Users;
import com.github.yourbootloader.domain.users.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UsersQueryService {

    private final UsersRepository usersRepository;

    public Users findOrElse() {
        return null;
    }
}

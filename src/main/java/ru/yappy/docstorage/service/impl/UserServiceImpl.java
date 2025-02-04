package ru.yappy.docstorage.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.*;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.yappy.docstorage.model.User;
import ru.yappy.docstorage.model.dto.*;
import ru.yappy.docstorage.out.repo.UserRepository;
import ru.yappy.docstorage.service.ConfirmationService;
import ru.yappy.docstorage.service.UserService;
import ru.yappy.docstorage.service.mapper.UserMapper;

@Slf4j
@Service
public class UserServiceImpl implements UserService {
    private final ConfirmationService confirmationService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserServiceImpl(ConfirmationService confirmationService,
                           UserRepository userRepository,
                           PasswordEncoder passwordEncoder) {
        this.confirmationService = confirmationService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Начало выполнения операции получения данных пользователя с именем {}", username);
        User user = userRepository.findByUsername(username).orElseThrow(() ->
                new UsernameNotFoundException("Пользователь с username='" + username + "' не был сохранен"));
        log.debug("Данные пользователя получены: {}", user);
        return user;
    }

    @Override
    public UserDto saveNewUser(NewUserDto newUserDto) {
        log.debug("Начало выполнения операции сохранения данных нового пользователя {}", newUserDto);
        User user = UserMapper.toModel(newUserDto);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user = userRepository.save(user);
        confirmationService.sendMessageWithTokenToNewUser(user);
        UserDto userDto = UserMapper.toDto(user);
        log.debug("Данные нового пользователя сохранены, присвоен идентификатор {}", user.getId());
        return userDto;
    }

    @Override
    public UserDetails getAuthenticatedUser() {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        Object principal = securityContext.getAuthentication().getPrincipal();
        return (principal instanceof UserDetails userDetails) ? userDetails : null;
    }

}
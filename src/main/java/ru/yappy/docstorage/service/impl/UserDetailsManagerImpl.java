package ru.yappy.docstorage.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.stereotype.Service;
import ru.yappy.docstorage.model.User;
import ru.yappy.docstorage.out.repo.UserRepository;

@Slf4j
@Service
public class UserDetailsManagerImpl implements UserDetailsManager {
    private final UserRepository userRepository;

    @Autowired
    public UserDetailsManagerImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
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
    public void createUser(UserDetails userDetails) {
        User userData = (User) userDetails;
        log.debug("Начало выполнения операции сохранения данных нового пользователя {}", userData);
        User newUser = userRepository.save(userData);
        log.debug("Данные нового пользователя сохранены, присвоен идентификатор {}", newUser.getId());
    }

    @Override
    public void updateUser(UserDetails user) {
        throw new UnsupportedOperationException("Функция обновления пользовательских данных не реализована");
    }

    @Override
    public void deleteUser(String username) {
        throw new UnsupportedOperationException("Функция удаления пользовательских данных не реализована");
    }

    @Override
    public void changePassword(String oldPassword, String newPassword) {
        throw new UnsupportedOperationException("Функция смены пароля пользователей не реализована");
    }

    @Override
    public boolean userExists(String username) {
        return userRepository.existsByUsername(username);
    }

    private boolean checkExistingByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

}
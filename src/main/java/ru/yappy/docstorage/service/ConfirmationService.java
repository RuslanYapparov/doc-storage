package ru.yappy.docstorage.service;

import ru.yappy.docstorage.model.User;
import ru.yappy.docstorage.model.dto.UserDto;

public interface ConfirmationService {

    void sendMessageWithTokenToNewUser(User user);

    UserDto confirmEmailAndEnableUser(String token);

}
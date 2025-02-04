package ru.yappy.docstorage.service.mapper;

import lombok.experimental.UtilityClass;
import ru.yappy.docstorage.model.User;
import ru.yappy.docstorage.model.dto.*;

@UtilityClass
public class UserMapper {

    public User toModel(NewUserDto newUserDto) {
        User user = new User();
        user.setUsername(newUserDto.username());
        user.setPassword(newUserDto.password());
        user.setEmail(newUserDto.email());
        user.setFirstName(newUserDto.firstName());
        user.setLastName(newUserDto.lastName());
        return user;
    }

    public UserDto toDto(User user) {
        return new UserDto(user.getUsername(),
                user.getFirstName(),
                user.getLastName(),
                user.isEnabled());
    }

}
package ru.yappy.docstorage.util;

import lombok.experimental.UtilityClass;
import ru.yappy.docstorage.model.User;
import ru.yappy.docstorage.model.dto.UserDto;

@UtilityClass
public class UserMapper {

    public User toModel(UserDto userDto) {
        User user = new User();
        user.setUsername(userDto.username());
        user.setPassword(userDto.password());
        user.setEmail(userDto.email());
        user.setFirstName(userDto.firstName());
        user.setLastName(userDto.lastName());
        return user;
    }

    public UserDto toDto(User user) {
        return new UserDto(user.getUsername(),
                user.getPassword(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName());
    }

}
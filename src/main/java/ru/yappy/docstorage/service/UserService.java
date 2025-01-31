package ru.yappy.docstorage.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import ru.yappy.docstorage.model.dto.UserDto;

public interface UserService extends UserDetailsService {

    UserDto saveNewUser(UserDto userDto);

    UserDto getUserDtoByUsername(String username);

    boolean checkUserExistingByUsername(String username);

    UserDetails getAuthenticatedUser();

}
package ru.yappy.docstorage.service;

import org.springframework.security.core.userdetails.*;
import ru.yappy.docstorage.model.dto.*;

public interface UserService extends UserDetailsService {

    UserDto saveNewUser(NewUserDto newUserDto);

    UserDto getUserDtoByUsername(String username);

    UserDetails getAuthenticatedUser();

}
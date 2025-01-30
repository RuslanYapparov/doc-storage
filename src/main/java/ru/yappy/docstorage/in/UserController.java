package ru.yappy.docstorage.in;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

import ru.yappy.docstorage.model.User;
import ru.yappy.docstorage.model.dto.UserDto;
import ru.yappy.docstorage.util.UserMapper;

@Slf4j
@RestController
@RequestMapping("/v1/users")
public class UserController {
    private final UserDetailsManager userDetailsManager;

    @Autowired
    public UserController(UserDetailsManager userDetailsManager) {
        this.userDetailsManager = userDetailsManager;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public void saveUser(@Valid @RequestBody UserDto userDto) {
        log.info("Поступил запрос на сохранение данных нового пользователя {}", userDto);
        User user = UserMapper.toModel(userDto);
        userDetailsManager.createUser(user);
        log.info("Пользователь {} успешно сохранен", user);
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Secured("ROLE_USER")
    public UserDto findAll() {
        return new UserDto("username", "password", "email", "name", "surname");
    }

}
package ru.yappy.docstorage.in;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

import ru.yappy.docstorage.model.dto.UserDto;
import ru.yappy.docstorage.service.UserService;

@Slf4j
@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public UserDto saveNewUser(@Valid@RequestBody UserDto userDto) {
        log.info("Поступил запрос на сохранение данных нового пользователя {}", userDto);
        userDto = userService.saveNewUser(userDto);
        log.info("Пользователь {} успешно сохранен", userDto);
        return userDto;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public UserDto getUserDtoById(@RequestParam(name = "username") @Valid @NotBlank String username) {
        log.info("Поступил запрос на получение данных пользователя с username='{}'", username);
        UserDto userDto = userService.getUserDtoByUsername(username);
        log.info("Данные пользователя с username='{}' найдены и отправляются клиенту", userDto);
        return userDto;
    }

}
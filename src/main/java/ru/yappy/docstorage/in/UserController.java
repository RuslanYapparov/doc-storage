package ru.yappy.docstorage.in;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

import ru.yappy.docstorage.model.dto.*;
import ru.yappy.docstorage.service.ConfirmationService;
import ru.yappy.docstorage.service.UserService;

@Slf4j
@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    private final UserService userService;
    private final ConfirmationService confirmationService;

    @Autowired
    public UserController(UserService userService,
                          ConfirmationService confirmationService) {
        this.userService = userService;
        this.confirmationService = confirmationService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public UserDto saveNewUser(@RequestBody @Valid NewUserDto newUserDto) {
        log.info("Поступил запрос на сохранение данных нового пользователя {}", newUserDto);
        UserDto userDto = userService.saveNewUser(newUserDto);
        log.info("Пользователь {} успешно сохранен", newUserDto);
        return userDto;
    }

    @GetMapping(value = "/confirm", produces = MediaType.APPLICATION_JSON_VALUE)
    public void confirmUser(@RequestParam(name = "token") @Valid @NotBlank String token) {
        log.info("Поступил запрос на подтверждение учетной записи пользователя по токену {}", token);
        UserDto userDto = confirmationService.confirmEmailAndEnableUser(token);
        log.info("Учетная запись пользователя '{}' успешно подтверждена", userDto.username());
    }

}
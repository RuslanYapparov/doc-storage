package ru.yappy.docstorage.in;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/users")
public class UserController {

    @GetMapping("/test")
    public String test() {
        return "test!";
    }

}

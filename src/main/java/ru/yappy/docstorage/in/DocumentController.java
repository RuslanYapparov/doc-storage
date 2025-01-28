package ru.yappy.docstorage.in;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/v1/docs")
@Valid
public class DocumentController {

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public String findAll() {
        log.info("Запрос на получение списка документов, сохраненных пользователем {}", "username");
        return "Список документов";
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public String findById(@PathVariable(name = "id") @Min(1) Long id) {
        log.info("Запрос на получение документа с id {}", id);
        return "Документ с идентификатором " + id;
    }

}
package ru.yappy.docstorage.in;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.yappy.docstorage.model.dto.DocumentDto;
import ru.yappy.docstorage.service.DocumentService;

import java.io.IOException;

@Slf4j
@RestController
@RequestMapping("/api/v1/docs")
@Valid
public class DocumentController {
    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @PostMapping(value = "/uploading", consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public DocumentDto uploadNewDocument(@RequestParam("file") MultipartFile file,
                                         @RequestParam("title") @NotBlank String title,
                                         @RequestParam("description") @NotBlank String description) throws IOException {
        String subDescription = (description.length() < 20) ? description : description.substring(0, 20) + "(...)";
        log.info("Получен запрос на загрузку документа с названием '{}' и описанием '{}'", title, subDescription);
        DocumentDto documentDto = documentService.saveNewDocument(file, title, description);
        log.info("Данные о документе и файл успешно сохранены.");
        return documentDto;
    }

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
package ru.yappy.docstorage.in;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.*;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.yappy.docstorage.model.User;
import ru.yappy.docstorage.model.dto.DocumentDto;
import ru.yappy.docstorage.model.paramholder.*;
import ru.yappy.docstorage.service.*;

import java.io.IOException;

@Slf4j
@RestController
@RequestMapping("/api/v1/docs")
@Valid
public class DocumentController {
    private final DocumentService documentService;
    private final UserService userService;

    public DocumentController(DocumentService documentService, UserService userService) {
        this.documentService = documentService;
        this.userService = userService;
    }

    @PostMapping(value = "/uploading", consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public DocumentDto uploadNewDocument(@RequestParam("file") MultipartFile file,
                                         @RequestParam("title") @NotBlank String title,
                                         @RequestParam("description") @NotBlank String description) throws IOException {
        String subDescription = (description.length() < 20) ? description : description.substring(0, 20) + "(...)";
        log.info("Получен запрос на сохранение документа с названием '{}' и описанием '{}'", title, subDescription);
        DocumentDto documentDto = documentService.saveNewDocument(file, title, description);
        log.info("Данные о документе и файл успешно сохранены.");
        return documentDto;
    }

    @GetMapping(value = "/owned", produces = MediaType.APPLICATION_JSON_VALUE)
    public DocumentDto[] getPageOfDocumentDtosSavedByUser(
            @RequestParam(name = "sortBy", defaultValue = "date") @NotBlank String sort,
            @RequestParam(name = "order", defaultValue = "desc") @NotBlank String order,
            @RequestParam(name = "from", defaultValue = "0") @Min(0) int from,
            @RequestParam(name = "size", defaultValue = "10") @Min(1) int size) {
        GetSavedDocsParamHolder getSavedDocsParamHolder = new GetSavedDocsParamHolder(
                DocSortType.valueOf(sort.toUpperCase()),
                Sort.Direction.valueOf(order.toUpperCase()),
                from,
                size);
        User user = (User) userService.getAuthenticatedUser();
        log.info("Получен запрос от пользователя '{}' на получение списка из '{}' сохраненных документов, " +
                        "отсортированных по '{}' в '{}' порядке, начиная с позиции '{}'.", user.getUsername(), size,
                sort, order, from);
        DocumentDto[] documentDtos = documentService.getSavedDocumentsWithParameters(getSavedDocsParamHolder);
        log.info("Список из '{}' сохраненных документов в порядке, заданном параметрами запроса, успешно получен.",
                documentDtos.length);
        return documentDtos;
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public Resource getDocumentById(@PathVariable("id") @Min(1) Long id) throws IOException {
        log.info("Получен запрос на загрузку файла документа с id={}", id);
        Resource docResource = documentService.getDocumentResourceById(id);
        log.info("Файл документа успешно загружен.");
        return docResource;
    }

}
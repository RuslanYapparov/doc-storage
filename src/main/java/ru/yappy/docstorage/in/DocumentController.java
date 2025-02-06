package ru.yappy.docstorage.in;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.*;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.yappy.docstorage.model.*;
import ru.yappy.docstorage.model.dto.DocumentDto;
import ru.yappy.docstorage.model.paramholder.*;
import ru.yappy.docstorage.service.*;

import java.io.IOException;
import java.time.LocalDate;

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
        User user = (User) userService.getAuthenticatedUser();
        String subDescription = (description.length() < 20) ? description : description.substring(0, 20) + "(...)";
        log.info("Получен запрос от пользователя '{}' на сохранение документа с названием '{}' и описанием '{}'",
                user.getUsername(), title, subDescription);
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
        GetDocsParamHolder getDocsParamHolder = new GetDocsParamHolder(
                DocSortType.valueOf(sort.toUpperCase()),
                Sort.Direction.valueOf(order.toUpperCase()),
                from,
                size);
        User user = (User) userService.getAuthenticatedUser();
        log.info("Получен запрос от пользователя '{}' на получение списка из '{}' сохраненных документов, " +
                        "отсортированных по '{}' в '{}' порядке, начиная с позиции '{}'.", user.getUsername(), size,
                sort, order, from);
        DocumentDto[] documentDtos = documentService.getSavedDocumentsWithParameters(getDocsParamHolder);
        log.info("Список из {} сохраненных документов пользователя '{}' успешно получен.", documentDtos.length,
                user.getUsername());
        return documentDtos;
    }

    @GetMapping(value = "/available", produces = MediaType.APPLICATION_JSON_VALUE)
    public DocumentDto[] getPageOfDocumentDtosAvailableForUser(
            @RequestParam(name = "sortBy", defaultValue = "date") @NotBlank String sort,
            @RequestParam(name = "order", defaultValue = "desc") @NotBlank String order,
            @RequestParam(name = "from", defaultValue = "0") @Min(0) int from,
            @RequestParam(name = "size", defaultValue = "10") @Min(1) int size,
            @RequestParam(name = "withOwned", defaultValue = "false") boolean withOwned,
            @RequestParam(name = "withSharedForAll", defaultValue = "true") boolean withSharedForAll) {
        GetDocsParamHolder getDocsParamHolder = new GetDocsParamHolder(
                DocSortType.valueOf(sort.toUpperCase()),
                Sort.Direction.valueOf(order.toUpperCase()),
                from,
                size,
                withOwned,
                withSharedForAll);
        User user = (User) userService.getAuthenticatedUser();
        log.info("Получен запрос от пользователя '{}' на получение списка из '{}' документов, доступных для " +
                        "загрузки, отсортированных по '{}' в '{}' порядке, начиная с позиции '{}'.",
                user.getUsername(), size, sort, order, from);
        DocumentDto[] documentDtos = documentService.getAvailableDocumentsWithParameters(getDocsParamHolder);
        log.info("Список из {} доступных для загрузки пользователю '{}' документов успешно получен.",
                documentDtos.length, user.getUsername());
        return documentDtos;
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public Resource getDocumentById(@PathVariable("id") @Min(1) Long id) throws IOException {
        log.info("Получен запрос на загрузку файла документа с id={}", id);
        Resource docResource = documentService.getDocumentResourceById(id);
        log.info("Файл документа успешно загружен.");
        return docResource;
    }

    @GetMapping(value = "/search/owned", produces = MediaType.APPLICATION_JSON_VALUE)
    public DocumentDto[] getPageOfSavedDocsSearchedWithParameters(
            @RequestParam(name = "searchFor", defaultValue = "") String searchFor,
            @RequestParam(name = "since", required = false) LocalDate since,
            @RequestParam(name = "until", required = false) LocalDate until,
            @RequestParam(name = "sortBy", defaultValue = "date") @NotBlank String sort,
            @RequestParam(name = "order", defaultValue = "desc") @NotBlank String order,
            @RequestParam(name = "from", defaultValue = "0") @Min(0) int from,
            @RequestParam(name = "size", defaultValue = "10") @Min(1) int size) {
        SearchInDocsParamHolder searchInDocsParamHolder = new SearchInDocsParamHolder(
                searchFor,
                since,
                until,
                DocSortType.valueOf(sort.toUpperCase()),
                Sort.Direction.valueOf(order.toUpperCase()),
                from,
                size);
        User user = (User) userService.getAuthenticatedUser();
        log.info("Получен запрос от пользователя '{}' на поиск в сохраненных документах с параметрами {}.",
                user.getUsername(), searchInDocsParamHolder.toStringForSavedDocs());
        DocumentDto[] documentDtos = documentService.searchInSavedDocumentsWithParameters(searchInDocsParamHolder);
        log.info("Список из {} сохраненных документов, найденных при поиске для пользователя '{}', успешно получен.",
                documentDtos.length, user.getUsername());
        return documentDtos;
    }

    @GetMapping(value = "/search/available", produces = MediaType.APPLICATION_JSON_VALUE)
    public DocumentDto[] getPageOfAvailableDocsSearchedWithParameters(
            @RequestParam(name = "searchFor", defaultValue = "") String searchFor,
            @RequestParam(name = "since", required = false) LocalDate since,
            @RequestParam(name = "until", required = false) LocalDate until,
            @RequestParam(name = "sortBy", defaultValue = "date") @NotBlank String sort,
            @RequestParam(name = "order", defaultValue = "desc") @NotBlank String order,
            @RequestParam(name = "from", defaultValue = "0") @Min(0) int from,
            @RequestParam(name = "size", defaultValue = "10") @Min(1) int size,
            @RequestParam(name = "withOwned", defaultValue = "true") boolean withOwned,
            @RequestParam(name = "withSharedForAll", defaultValue = "true") boolean withSharedForAll) {
        SearchInDocsParamHolder searchInDocsParamHolder = new SearchInDocsParamHolder(
                searchFor,
                since,
                until,
                DocSortType.valueOf(sort.toUpperCase()),
                Sort.Direction.valueOf(order.toUpperCase()),
                from,
                size,
                withOwned,
                withSharedForAll);
        User user = (User) userService.getAuthenticatedUser();
        log.info("Получен запрос от пользователя '{}' на поиск в доступных документах с параметрами {}.",
                user.getUsername(), searchInDocsParamHolder);
        DocumentDto[] documentDtos = documentService.searchInAvailableDocumentsWithParameters(searchInDocsParamHolder);
        log.info("Список из {} доступных документов, найденных при поиске для пользователя '{}', успешно получен.",
                documentDtos.length, user.getUsername());
        return documentDtos;
    }

    @PatchMapping(value = "/updating/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public DocumentDto updateDocument(@PathVariable(name = "id") @Min(1) Long id,
                                      @RequestParam(name = "file", required = false) MultipartFile file,
                                      @RequestParam(name = "title", required = false) String title,
                                      @RequestParam(name = "description", required = false) String description)
            throws IOException {
        User user = (User) userService.getAuthenticatedUser();
        String subDescription = (description.length() < 20) ? description : description.substring(0, 20) + "(...)";
        log.info("Получен запрос от пользователя '{}' на обновление документа с id={}. " +
                        "Переданные параметры: название '{}', описание '{}', наличие файла '{}'",
                user.getUsername(), id, title, subDescription, file != null);
        DocumentDto documentDto = documentService.updateEditedDocument(file, id, title, description);
        log.info("Данные о документе и файл успешно обновлены.");
        return documentDto;
    }

    @PatchMapping(value = "/share/open/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public DocumentDto shareDocumentForAll(@PathVariable("id") @Min(1) Long id,
                                           @RequestParam("accessType") String accessString) {
        AccessType accessType = AccessType.valueOf(accessString.toUpperCase());
        log.info("Получен запрос на открытие '{}' общего доступа к документу с id={}", accessType, id);
        DocumentDto documentDto = documentService.shareDocumentForAllUsers(id, accessType);
        log.info("Общий доступ с типом '{}' к документу с id={} успешно открыт.", accessType, id);
        return documentDto;
    }

    @PatchMapping(value = "/share/close/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public DocumentDto closeSharedAccessToDocument(@PathVariable("id") @Min(1) Long id) {
        log.info("Получен запрос на закрытие общего доступа к документу с id={}", id);
        DocumentDto documentDto = documentService.closeSharedAccessToDocument(id);
        log.info("Общий доступ к документу с id={} успешно закрыт.", id);
        return documentDto;
    }

    @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public DocumentDto deleteDocument(@PathVariable("id") @Min(1) Long id) throws IOException {
        User user = (User) userService.getAuthenticatedUser();
        log.info("Получен запрос от пользователя '{}' на удаление документа с id={}", user.getUsername(), id);
        DocumentDto docDto = documentService.deleteDocument(id);
        log.info("Документ с id={} успешно удален.", id);
        return docDto;
    }

}
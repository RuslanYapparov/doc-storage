package ru.yappy.docstorage.in;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yappy.docstorage.model.User;
import ru.yappy.docstorage.model.dto.DocUserAccessDto;
import ru.yappy.docstorage.service.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/accesses")
public class DocUserAccessController {
    private final DocUserAccessService docUserAccessService;
    private final UserService userService;

    @Autowired
    public DocUserAccessController(DocUserAccessService docUserAccessService,
                                   UserService userService) {
        this.docUserAccessService = docUserAccessService;
        this.userService = userService;
    }

    @PostMapping(produces = "application/json")
    public DocUserAccessDto saveDocUserAccess(@RequestBody @Valid DocUserAccessDto docUserAccessDto) {
        User user = (User) userService.getAuthenticatedUser();
        log.info("Поступил запрос от пользователя '{}' на предоставление доступа к документу {}.", user.getUsername(),
                docUserAccessDto);
        DocUserAccessDto dto = docUserAccessService.grantAccessToDocumentForUser(docUserAccessDto);
        log.info("Данные о доступе '{}' пользователя '{}' к документу с id={} успешно сохранены",
                docUserAccessDto.accessType(), user.getUsername(), docUserAccessDto.docId());
        return dto;
    }

    @DeleteMapping
    public void deleteDocUserAccess(@RequestParam(name = "docId") @Valid @Min(1) Long docId,
                                    @RequestParam(name = "username") @Valid @NotBlank String username) {
        User user = (User) userService.getAuthenticatedUser();
        log.info("Поступил запрос от пользователя '{}' на отзыв доступа к документу с id={}  пользователя '{}'.",
                user.getUsername(), docId, username);
        docUserAccessService.revokeAccessToDocumentForUser(docId, username);
        log.info("Данные о доступе пользователя '{}' к документу с id={} успешно удалены", username, docId);
    }

}
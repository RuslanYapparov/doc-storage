package ru.yappy.docstorage.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yappy.docstorage.model.*;
import ru.yappy.docstorage.model.dto.DocUserAccessDto;
import ru.yappy.docstorage.out.repo.*;
import ru.yappy.docstorage.service.*;
import ru.yappy.docstorage.service.mapper.DocUserAccessMapper;

@Slf4j
@Service
public class DocUserAccessServiceImpl implements DocUserAccessService {
    private final UserService userService;
    private final DocUserAccessRepository docUserAccessRepository;
    private final DocumentRepository documentRepository;

    @Autowired
    public DocUserAccessServiceImpl(UserService userService,
                                    DocUserAccessRepository docUserAccessRepository,
                                    DocumentRepository documentRepository) {
        this.userService = userService;
        this.docUserAccessRepository = docUserAccessRepository;
        this.documentRepository = documentRepository;
    }

    @Override
    public DocUserAccess saveAccessToDocumentForOwner(Long docId, String usernameOfRecipient) {
        log.debug("Начало выполнения операции сохранения прав обладателя на документ с id={} для пользователя '{}'.",
                docId, usernameOfRecipient);
        DocUserAccess newAccess =
                docUserAccessRepository.save(new DocUserAccess(docId, usernameOfRecipient, AccessType.REMOVE));
        log.debug("Пользователь '{}' теперь имеет доступ '{}' к документу с id={}.",
                usernameOfRecipient, AccessType.REMOVE, docId);
        return newAccess;
    }

    @Override
    public boolean checkUserAccessToDocument(Long docId, String username) {
        log.debug("Проверка доступа пользователя '{}' к документу с id={}.", username, docId);
        boolean haveAccess = docUserAccessRepository.existsByDocIdAndUsername(docId, username);
        log.debug("Пользователь '{}' {} имеет доступ к документу с id='{}'.", username, haveAccess ? "" : "НЕ", docId);
        return haveAccess;
    }

    @Override
    public DocUserAccessDto grantAccessToDocumentForUser(DocUserAccessDto dto) {
        log.debug("Начало выполнения операции сохранения новых пользовательских прав '{}' на документ с id='{}' " +
                "для пользователя '{}'.", dto.accessType(), dto.docId(), dto.username());
        User owner = (User) userService.getAuthenticatedUser();
        if (!owner.getUsername().equals(documentRepository.findOwnerUsernameByDocumentId(dto.docId()))) {
            throw new IllegalArgumentException(String.format("Пользователь '%s' не является обладателем " +
                    "документа с id='%d' и не может предоставить доступ другим.", owner.getUsername(), dto.docId()));
        }
        DocUserAccessDto docUserAccessDto = DocUserAccessMapper.toDto(
                docUserAccessRepository.save(new DocUserAccess(dto.docId(), dto.username(), dto.accessType())));
        log.debug("Пользователь '{}' предоставил доступ '{}' пользователю '{}' к документу с id='{}'.",
                owner.getUsername(), dto.accessType(), dto.username(), dto.docId());
        return docUserAccessDto;
    }

    @Override
    public void revokeAccessToDocumentForUser(Long docId, String usernameOfRevoked) {
        log.debug("Начало выполнения операции отзыва права доступа к документу с id='{}' пользователю '{}'.",
                docId, usernameOfRevoked);
        User owner = (User) userService.getAuthenticatedUser();
        if (!owner.getUsername().equals(documentRepository.findOwnerUsernameByDocumentId(docId))) {
            throw new IllegalArgumentException(String.format("Пользователь '%s' не является обладателем " +
                    "документа с id='%d' и не может отозвать доступ.", owner.getUsername(), docId));
        }
        docUserAccessRepository.deleteByDocIdAndUsername(docId, usernameOfRevoked);
        log.debug("Пользователь '{}' отозвал доступ пользователя '{}' к документу с id='{}'.",
                owner.getUsername(), usernameOfRevoked, docId);
    }

}
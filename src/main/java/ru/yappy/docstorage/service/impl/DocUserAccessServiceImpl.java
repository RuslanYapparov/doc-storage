package ru.yappy.docstorage.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.ObjectNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yappy.docstorage.model.*;
import ru.yappy.docstorage.model.dto.DocUserAccessDto;
import ru.yappy.docstorage.out.repo.*;
import ru.yappy.docstorage.service.*;
import ru.yappy.docstorage.service.mapper.DocUserAccessMapper;

import java.util.Optional;

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
    public void saveAccessToDocumentForOwner(Long docId, String usernameOfRecipient) {
        log.debug("Начало выполнения операции сохранения прав обладателя на документ с id={} для пользователя '{}'.",
                docId, usernameOfRecipient);
        docUserAccessRepository.save(new DocUserAccess(docId, usernameOfRecipient, AccessType.REMOVE));
        log.debug("Пользователь '{}' теперь имеет доступ '{}' к документу с id={}.",
                usernameOfRecipient, AccessType.REMOVE, docId);
    }

    @Override
    public DocUserAccessDto grantAccessToDocumentForUser(DocUserAccessDto dto) {
        log.debug("Начало выполнения операции сохранения новых пользовательских прав '{}' на документ с id='{}' " +
                "для пользователя '{}'.", dto.accessType(), dto.docId(), dto.username());
        User owner = (User) userService.getAuthenticatedUser();
        checkDocumentForOwner(dto.docId(), owner.getUsername());
        if (owner.getUsername().equals(dto.username())) {
            throw new IllegalArgumentException("Обладатель документа имеет полный контроль над ним и не " +
                    "может это изменить.");
        }
        DocUserAccessDto docUserAccessDto = DocUserAccessMapper.toDto(
                docUserAccessRepository.save(new DocUserAccess(dto.docId(), dto.username(), dto.accessType())));
        log.debug("Пользователь '{}' предоставил доступ '{}' пользователю '{}' к документу с id='{}'.",
                owner.getUsername(), dto.accessType(), dto.username(), dto.docId());
        return docUserAccessDto;
    }

    @Override
    public Document getCheckedDocumentForOperations(AccessType operationType, Long docId, String username) {
        Document document = documentRepository.findById(docId)
                .orElseThrow(() -> new ObjectNotFoundException(docId, "Document"));
        Optional<AccessType> grantedAccessType =
                docUserAccessRepository.findAccessTypeByDocIdAndUsername(docId, username);
        switch (operationType) {
            case READ_ONLY -> {
                if (document.getCommonAccessType() == null && grantedAccessType.isEmpty()) {
                    throw new IllegalArgumentException(String.format("Документ с id=%d не доступен для скачивания " +
                            "пользователю '%s'", document.getId(), username));
                }
            }
            case EDIT -> {
                boolean haveNotCommonAccess = document.getCommonAccessType() == null ||
                        AccessType.READ_ONLY.equals(document.getCommonAccessType());
                boolean haveNotGrantedAccess = grantedAccessType.isEmpty() ||
                        AccessType.READ_ONLY.equals(grantedAccessType.get());
                if (haveNotCommonAccess && haveNotGrantedAccess) {
                    throw new IllegalArgumentException(String.format("Документ с id=%d не доступен для обновления " +
                            "пользователю '%s'", document.getId(), username));
                }
            }
            case REMOVE -> {
                boolean haveNotGrantedAccess = grantedAccessType.isEmpty() ||
                        !AccessType.REMOVE.equals(grantedAccessType.get());
                if (!AccessType.REMOVE.equals(document.getCommonAccessType()) && haveNotGrantedAccess) {
                    throw new IllegalArgumentException(String.format("Документ с id=%d не доступен для удаления " +
                            "пользователю '%s'", document.getId(), username));
                }
            }
        }
        return document;
    }

    @Override
    public String[] getUsernamesWithGrantedAccess(Long docId, AccessType accessType) {
        String logVariant = (accessType == null) ? "c доступом" : ("с типом доступа " + accessType);
        log.debug("Начало выполнения операции поиска имен пользователей {} к документу с id={}.", logVariant, docId);
        String[] usernames = (accessType == null) ?
                docUserAccessRepository.findUsernameByDocId(docId) :
                docUserAccessRepository.findUsernameByDocIdAndAccessType(docId, accessType);
        log.debug("Найдено {} имен пользователей {} к документу с id={}.", usernames.length, logVariant, docId);
        return usernames;
    }

    @Override
    public void revokeAccessToDocumentForUser(Long docId, String usernameOfRevoked) {
        log.debug("Начало выполнения операции отзыва права доступа к документу с id='{}' пользователю '{}'.",
                docId, usernameOfRevoked);
        User owner = (User) userService.getAuthenticatedUser();
        checkDocumentForOwner(docId, owner.getUsername());
        if (owner.getUsername().equals(usernameOfRevoked)) {
            throw new IllegalArgumentException("Обладатель документа не может отозвать доступ к " +
                    "нему у самого себя.");
        }
        docUserAccessRepository.deleteByDocIdAndUsername(docId, usernameOfRevoked);
        log.debug("Пользователь '{}' отозвал доступ пользователя '{}' к документу с id='{}'.",
                owner.getUsername(), usernameOfRevoked, docId);
    }

    private void checkDocumentForOwner(Long docId, String username) {
        if (!username.equals(documentRepository.findOwnerUsernameByDocumentId(docId))) {
            throw new IllegalArgumentException(String.format("Пользователь '%s' не является обладателем " +
                    "документа с id='%d' и не может управлять доступом к нему.", username, docId));
        }
    }

}
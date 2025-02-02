package ru.yappy.docstorage.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yappy.docstorage.model.AccessType;
import ru.yappy.docstorage.model.DocUserAccess;
import ru.yappy.docstorage.out.repo.DocUserAccessRepository;
import ru.yappy.docstorage.service.DocUserAccessService;

@Slf4j
@Service
public class DocUserAccessServiceImpl implements DocUserAccessService {
    private final DocUserAccessRepository docUserAccessRepository;

    @Autowired
    public DocUserAccessServiceImpl(DocUserAccessRepository docUserAccessRepository) {
        this.docUserAccessRepository = docUserAccessRepository;
    }

    @Override
    public DocUserAccess grantAccessToDocumentForUser(Long docId, String usernameOfRecipient, AccessType accessType) {
        log.debug("Начало выполнения операции сохранения новых пользовательских прав '{}' на документ с id='{}'" +
                " для пользователя '{}'.", accessType, docId, usernameOfRecipient);
        DocUserAccess newAccess =
                docUserAccessRepository.save(new DocUserAccess(docId, usernameOfRecipient, accessType));
        log.debug("Пользователю '{}' предоставлено право '{}' на документ с id='{}'.",
                usernameOfRecipient, accessType, docId);
        return newAccess;
    }

    @Override
    public boolean checkUserAccessToDocument(Long docId, String username) {
        log.debug("Проверка доступа пользователя '{}' к документу с id={}.", username, docId);
        boolean haveAccess = docUserAccessRepository.existsByDocIdAndUsername(docId, username);
        log.debug("Пользователь '{}' {} имеет доступ к документу с id='{}'.", username, haveAccess ? "" : "НЕ", docId);
        return haveAccess;
    }

}
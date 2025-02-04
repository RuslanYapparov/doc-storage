package ru.yappy.docstorage.service;

import ru.yappy.docstorage.model.*;
import ru.yappy.docstorage.model.dto.DocUserAccessDto;

public interface DocUserAccessService {

    void saveAccessToDocumentForOwner(Long docId, String usernameOfRecipient);

    DocUserAccessDto grantAccessToDocumentForUser(DocUserAccessDto docUserAccessDto);

    Document getCheckedDocumentForOperations(AccessType operationType, Long docId, String username);

    String[] getUsernamesWithGrantedAccess(Long docId, AccessType accessType);

    void revokeAccessToDocumentForUser(Long docId, String usernameOfRevoked);

}
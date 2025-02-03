package ru.yappy.docstorage.service;

import ru.yappy.docstorage.model.*;
import ru.yappy.docstorage.model.dto.DocUserAccessDto;

public interface DocUserAccessService {

    DocUserAccess saveAccessToDocumentForOwner(Long docId, String usernameOfRecipient);

    boolean checkUserAccessToDocument(Long docId, String username);

    boolean checkUserAccessToDocument(Long docId, String username, AccessType accessType);

    DocUserAccessDto grantAccessToDocumentForUser(DocUserAccessDto docUserAccessDto);

    void revokeAccessToDocumentForUser(Long docId, String usernameOfRevoked);

}
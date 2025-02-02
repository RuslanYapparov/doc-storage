package ru.yappy.docstorage.service;

import ru.yappy.docstorage.model.*;

public interface DocUserAccessService {

    DocUserAccess grantAccessToDocumentForUser(Long docId, String usernameOfRecipient, AccessType accessType);

    boolean checkUserAccessToDocument(Long docId, String username);

}
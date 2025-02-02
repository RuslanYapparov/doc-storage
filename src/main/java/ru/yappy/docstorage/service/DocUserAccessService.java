package ru.yappy.docstorage.service;

import ru.yappy.docstorage.model.AccessType;
import ru.yappy.docstorage.model.DocUserAccess;

public interface DocUserAccessService {

    DocUserAccess grantAccessToDocumentForUser(Long docId, String usernameOfRecipient, AccessType accessType);

}
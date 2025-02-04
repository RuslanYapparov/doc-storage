package ru.yappy.docstorage.service;

import ru.yappy.docstorage.model.*;
import ru.yappy.docstorage.model.dto.DocUserAccessDto;

public interface DocUserAccessService {

    DocUserAccess saveAccessToDocumentForOwner(Long docId, String usernameOfRecipient);

    DocUserAccessDto grantAccessToDocumentForUser(DocUserAccessDto docUserAccessDto);

    void revokeAccessToDocumentForUser(Long docId, String usernameOfRevoked);

}
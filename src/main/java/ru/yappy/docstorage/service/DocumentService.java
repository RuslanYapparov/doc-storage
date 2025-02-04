package ru.yappy.docstorage.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;
import ru.yappy.docstorage.model.AccessType;
import ru.yappy.docstorage.model.dto.DocumentDto;
import ru.yappy.docstorage.model.paramholder.*;

import java.io.*;

public interface DocumentService {

    DocumentDto saveNewDocument(MultipartFile file, String title, String description) throws IOException;

    Resource getDocumentResourceById(Long id) throws IOException;

    DocumentDto[] getSavedDocumentsWithParameters(GetDocsParamHolder paramHolder);

    DocumentDto[] getAvailableDocumentsWithParameters(GetDocsParamHolder paramHolder);

    DocumentDto[] searchInSavedDocumentsWithParameters(SearchInDocsParamHolder paramHolder);

    DocumentDto[] searchInAvailableDocumentsWithParameters(SearchInDocsParamHolder paramHolder);

    DocumentDto updateEditedDocument(MultipartFile file, Long docId, String title, String description) throws IOException;

    DocumentDto shareDocumentForAllUsers(Long docId, AccessType accessType);

    DocumentDto closeSharedAccessToDocument(Long docId);

    DocumentDto deleteDocument(Long docId) throws IOException;

}
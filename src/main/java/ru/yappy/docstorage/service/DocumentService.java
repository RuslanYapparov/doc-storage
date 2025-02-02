package ru.yappy.docstorage.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;
import ru.yappy.docstorage.model.dto.DocumentDto;
import ru.yappy.docstorage.model.paramholder.GetDocsParamHolder;

import java.io.*;

public interface DocumentService {

    DocumentDto saveNewDocument(MultipartFile file, String title, String description) throws IOException;

    Resource getDocumentResourceById(Long id) throws IOException;

    DocumentDto[] getSavedDocumentsWithParameters(GetDocsParamHolder paramHolder);

    DocumentDto[] getAvailableDocumentsWithParameters(GetDocsParamHolder paramHolder);

}
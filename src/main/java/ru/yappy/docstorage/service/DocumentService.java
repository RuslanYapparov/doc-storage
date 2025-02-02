package ru.yappy.docstorage.service;

import org.springframework.web.multipart.MultipartFile;
import ru.yappy.docstorage.model.dto.DocumentDto;

import java.io.IOException;

public interface DocumentService {

    DocumentDto saveNewDocument(MultipartFile file, String title, String description) throws IOException;

}
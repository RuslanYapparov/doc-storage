package ru.yappy.docstorage.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.yappy.docstorage.model.*;
import ru.yappy.docstorage.model.dto.DocumentDto;
import ru.yappy.docstorage.out.repo.DocumentRepository;
import ru.yappy.docstorage.service.*;
import ru.yappy.docstorage.service.mapper.DocumentMapper;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Set;

@Slf4j
@Service
public class DocumentServiceImpl implements DocumentService {
    private final UserService userService;
    private final DocUserAccessService docUserAccessService;
    private final FileManager fileManager;
    private final DocumentRepository documentRepository;

    @Autowired
    public DocumentServiceImpl(UserService userService,
                               DocUserAccessService docUserAccessService,
                               FileManager fileManager,
                               DocumentRepository documentRepository) {
        this.userService = userService;
        this.docUserAccessService = docUserAccessService;
        this.fileManager = fileManager;
        this.documentRepository = documentRepository;
    }

    @Override
    public DocumentDto saveNewDocument(MultipartFile file, String title, String description) throws IOException {
        log.debug("Начало операции сохранения данных о документе в базе и файла документа в хранилище.");
        User owner = (User) userService.getAuthenticatedUser();
        Path filePath = fileManager.saveFile(file);

        Document document = new Document();
        document.setTitle(title);
        document.setDescription(description);
        document.setOwner(owner);
        document.setFilePath(filePath.toString());
        document.setCreatedAt(LocalDate.now());
        document = documentRepository.save(document);

        document.setFilePath(filePath.getFileName().toString());
        document.setUsersWithAccess(Set.of(docUserAccessService.grantAccessToDocumentForUser(document.getId(),
                owner.getUsername(), AccessType.REMOVE)));
        DocumentDto documentDto = DocumentMapper.toDto(document);
        log.debug("Данные о документе успешно сохранены в базе, а файл документа в хранилище.");
        return documentDto;
    }

}
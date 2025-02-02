package ru.yappy.docstorage.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.ObjectNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.yappy.docstorage.model.*;
import ru.yappy.docstorage.model.dto.DocumentDto;
import ru.yappy.docstorage.model.paramholder.GetSavedDocsParamHolder;
import ru.yappy.docstorage.out.repo.DocumentRepository;
import ru.yappy.docstorage.service.*;
import ru.yappy.docstorage.service.mapper.DocumentMapper;

import java.io.IOException;
import java.nio.file.*;
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

    @Override
    public Resource getDocumentResourceById(Long id) throws IOException {
        User user = (User) userService.getAuthenticatedUser();
        log.debug("Начало операции получения файла документа из хранилища для пользователя '{}'", user.getUsername());
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException(id, "Document"));
        if (!document.isSharedForAll() ||
                !docUserAccessService.checkUserAccessToDocument(document.getId(), user.getUsername())) {
            throw new IllegalArgumentException(String.format("Документ с id=%d не доступен для пользователя '%s'",
                    id, user.getUsername()));
        }
        Path docPath = Paths.get(document.getFilePath());
        InputStreamResource docResource = new InputStreamResource(fileManager.getDocumentInputStream(docPath));
        log.debug("Данные о документе найдены в базе, файл документа подготовлен для отправки.");
        return docResource;
    }

    @Override
    public DocumentDto[] getSavedDocumentsWithParameters(GetSavedDocsParamHolder paramHolder) {
        User user = (User) userService.getAuthenticatedUser();
        log.debug("Начало операции получения сохраненных документов пользователя '{}' с параметрами {}.",
                user.getUsername(), paramHolder);
        Pageable page = PageRequest.of((paramHolder.from() / paramHolder.size()), paramHolder.size(),
                paramHolder.order(), paramHolder.sortBy().getPropertyName());
        DocumentDto[] docDtos = documentRepository.findAllByOwnerId(user.getId(), page).stream()
                .map(DocumentMapper::toDto)
                .toArray(DocumentDto[]::new);
        log.debug("Данные о {} сохраненных документах пользователя '{}', начиная с позиции '{}', получены из базы.",
                docDtos.length, user.getUsername(), paramHolder.from());
        return docDtos;
    }

}
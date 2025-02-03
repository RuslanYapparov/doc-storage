package ru.yappy.docstorage.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.ObjectNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.*;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.yappy.docstorage.model.*;
import ru.yappy.docstorage.model.dto.DocumentDto;
import ru.yappy.docstorage.model.paramholder.*;
import ru.yappy.docstorage.out.repo.DocumentRepository;
import ru.yappy.docstorage.service.*;
import ru.yappy.docstorage.service.mapper.DocumentMapper;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Stream;

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
        document.setUsersWithAccess(Set.of(docUserAccessService.saveAccessToDocumentForOwner(document.getId(),
                owner.getUsername())));
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
    public DocumentDto[] getSavedDocumentsWithParameters(GetDocsParamHolder paramHolder) {
        User user = (User) userService.getAuthenticatedUser();
        log.debug("Начало операции получения сохраненных документов пользователя '{}' с параметрами {}.",
                user.getUsername(), paramHolder.toStringForSavedDocs());
        Pageable page = PageRequest.of((paramHolder.from() / paramHolder.size()),
                paramHolder.size(),
                paramHolder.order(),
                paramHolder.sortBy().getPropertyName()
        );
        Stream<Document> docStream = documentRepository.findAllByOwnerId(user.getId(), page).stream();
        DocumentDto[] docDtos = DocumentMapper.toDtoArray(docStream);
        log.debug("Данные о {} сохраненных документах пользователя '{}', начиная с позиции '{}', получены из базы.",
                docDtos.length, user.getUsername(), paramHolder.from());
        return docDtos;
    }

    @Override
    public DocumentDto[] getAvailableDocumentsWithParameters(GetDocsParamHolder paramHolder) {
        User user = (User) userService.getAuthenticatedUser();
        log.debug("Начало операции получения доступных пользователю '{}' документов с параметрами {}.",
                user.getUsername(), paramHolder);
        Pageable page = PageRequest.of((paramHolder.from() / paramHolder.size()),
                paramHolder.size(),
                paramHolder.order(),
                paramHolder.sortBy().getPropertyName()
        );
        boolean withShared = paramHolder.withSharedForAll();
        Stream<Document> docStream = paramHolder.withOwned() ?
                documentRepository.findAllByIsSharedForAllOrOwnerIdOrUsersWithAccessUsername(withShared,
                        user.getId(), user.getUsername(), page).stream() :
                documentRepository.findAllByUsersWithAccessUsername(user.getUsername(), page).stream();
        DocumentDto[] docDtos = DocumentMapper.toDtoArray(docStream);
        log.debug("Данные о {} доступных пользователю '{}' документах, начиная с позиции '{}', получены из базы.",
                docDtos.length, user.getUsername(), paramHolder.from());
        return docDtos;
    }

    @Override
    public DocumentDto[] searchInSavedDocumentsWithParameters(SearchInDocsParamHolder paramHolder) {
        return new DocumentDto[0];
    }

    @Override
    public DocumentDto[] searchInAvailableDocumentsWithParameters(SearchInDocsParamHolder paramHolder) {
        return new DocumentDto[0];
    }

    @Override
    public DocumentDto updateEditedDocument(MultipartFile file, Long docId, String title, String description)
            throws IOException {

        return null;
    }

    @Override
    public DocumentDto shareDocumentForAllUsers(Long docId, AccessType accessType) {
        return null;
    }

    @Override
    public DocumentDto deleteDocument(Long docId) throws IOException {
        return null;
    }

}
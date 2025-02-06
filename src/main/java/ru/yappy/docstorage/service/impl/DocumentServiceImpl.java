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
import java.time.*;
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
        docUserAccessService.saveAccessToDocumentForOwner(document.getId(), owner.getUsername());
        DocumentDto documentDto = DocumentMapper.toDto(document);
        log.debug("Данные о документе успешно сохранены в базе, а файл документа в хранилище.");
        return documentDto;
    }

    @Override
    public Resource getDocumentResourceById(Long id) throws IOException {
        User user = (User) userService.getAuthenticatedUser();
        log.debug("Начало операции получения файла документа из хранилища для пользователя '{}'", user.getUsername());
        Document document =
                docUserAccessService.getCheckedDocumentForOperations(AccessType.READ_ONLY, id, user.getUsername());
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
        Pageable page = makePageableWithParameters(paramHolder);
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
        Pageable page = makePageableWithParameters(paramHolder);
        Page<Document> documents;
        if (paramHolder.withOwned()) {
            documents = paramHolder.withSharedForAll() ?
                    documentRepository.findAllAvailable(user.getUsername(), page) :
                    documentRepository.findAllAvailableWithoutShared(user.getUsername(), page);
        } else {
            documents = paramHolder.withSharedForAll() ?
                    documentRepository.findAllAvailableWithoutOwned(user.getUsername(), user.getId(), page) :
                    documentRepository.findAllAvailableWithoutOwnedAndShared(user.getUsername(), user.getId(), page);
        }
        DocumentDto[] docDtos = DocumentMapper.toDtoArray(documents.stream());
        log.debug("Данные о {} доступных пользователю '{}' документах, начиная с позиции '{}', получены из базы.",
                docDtos.length, user.getUsername(), paramHolder.from());
        return docDtos;
    }

    @Override
    public DocumentDto[] searchInSavedDocumentsWithParameters(SearchInDocsParamHolder paramHolder) {
        User user = (User) userService.getAuthenticatedUser();
        log.debug("Начало операции поиска в сохраненных документах пользователя '{}' с параметрами {}.",
                user.getUsername(), paramHolder.toStringForSavedDocs());
        Pageable page = makePageableWithParameters(paramHolder);
        Stream<Document> docStream = documentRepository.searchInSaved(user.getId(),
                paramHolder.searchFor(), paramHolder.since(), paramHolder.until(), page).stream();
        DocumentDto[] docDtos = DocumentMapper.toDtoArray(docStream);
        log.debug("Данные о {} результатах поиска в сохраненных документах пользователя '{}', " +
                        "начиная с позиции '{}', получены из базы.",
                docDtos.length, user.getUsername(), paramHolder.from());
        return docDtos;
    }

    @Override
    public DocumentDto[] searchInAvailableDocumentsWithParameters(SearchInDocsParamHolder paramHolder) {
        User user = (User) userService.getAuthenticatedUser();
        log.debug("Начало операции поиска в доступных для пользователя '{}' документах с параметрами {}.",
                user.getUsername(), paramHolder);
        Pageable page = makePageableWithParameters(paramHolder);
        Page<Document> documents;
        if (paramHolder.withOwned()) {
            documents = paramHolder.withSharedForAll() ?
                    documentRepository.searchInAllAvailable(user.getUsername(),
                            paramHolder.searchFor(), paramHolder.since(), paramHolder.until(), page) :
                    documentRepository.searchInAllAvailableWithoutShared(user.getUsername(),
                            paramHolder.searchFor(), paramHolder.since(), paramHolder.until(), page);
        } else {
            documents = paramHolder.withSharedForAll() ?
                    documentRepository.searchInAllAvailableWithoutOwned(user.getUsername(), user.getId(),
                            paramHolder.searchFor(), paramHolder.since(), paramHolder.until(), page) :
                    documentRepository.searchInAllAvailableWithoutOwnedAndShared(user.getUsername(), user.getId(),
                            paramHolder.searchFor(), paramHolder.since(), paramHolder.until(), page);
        }
        DocumentDto[] docDtos = DocumentMapper.toDtoArray(documents.stream());
        log.debug("Данные о {} результатах поиска в доступных для пользователя '{}' документах, " +
                        "начиная с позиции '{}', получены из базы.",
                docDtos.length, user.getUsername(), paramHolder.from());
        return docDtos;
    }

    @Override
    public DocumentDto updateEditedDocument(MultipartFile file, Long docId, String title, String description)
            throws IOException {
        log.debug("Начало операции обновления данных о документе в базе и файла документа в хранилище.");
        User user = (User) userService.getAuthenticatedUser();
        Document document =
                docUserAccessService.getCheckedDocumentForOperations(AccessType.EDIT, docId, user.getUsername());
        if (file != null) {
            fileManager.updateFile(file, Paths.get(document.getFilePath()));
        }
        document.setTitle(title == null ? document.getTitle() : title);
        document.setDescription(description == null ? document.getDescription() : description);
        document.setUpdatedBy(user.getUsername());
        document.setUpdatedAt(LocalDateTime.now());
        document = documentRepository.save(document);
        DocumentDto documentDto = DocumentMapper.toDto(document);
        log.debug("Данные о документе успешно обновлены в базе, а файл документа в хранилище.");
        return documentDto;
    }

    @Override
    public DocumentDto shareDocumentForAllUsers(Long docId, AccessType accessType) {
        log.debug("Начало выполнения операции открытия общего доступа с типом '{}' к документу с id={} для всех " +
                "пользователей.", accessType, docId);
        User user = (User) userService.getAuthenticatedUser();
        Document document = getCheckedForOwnerDocument(docId, user.getUsername());
        if (accessType.equals(document.getCommonAccessType())) {
            throw new IllegalArgumentException(String.format("Документ с id=%d уже открыт с доступом '%s' " +
                    "для всех пользователей.", docId, document.getCommonAccessType()));
        }
        document.setCommonAccessType(accessType);
        document = documentRepository.save(document);
        DocumentDto documentDto = DocumentMapper.toDto(document);
        log.debug("Документ с id={} теперь открыт с доступом '{}' для всех пользователей.", docId, accessType);
        return documentDto;
    }

    @Override
    public DocumentDto closeSharedAccessToDocument(Long docId) {
        log.debug("Начало выполнения операции закрытия общего доступа к документу с id={} для всех " +
                "пользователей.", docId);
        User user = (User) userService.getAuthenticatedUser();
        Document document = getCheckedForOwnerDocument(docId, user.getUsername());
        if (document.getCommonAccessType() == null) {
            throw new IllegalArgumentException(String.format("Документ с id=%d уже закрыт для всех пользователей.",
                    docId));
        }
        document.setCommonAccessType(null);
        document = documentRepository.save(document);
        DocumentDto documentDto = DocumentMapper.toDto(document);
        log.debug("Документ с id={} теперь не имеет общего доступа для всех пользователей.", docId);
        return documentDto;
    }

    @Override
    public DocumentDto deleteDocument(Long docId) throws IOException {
        log.debug("Начало операции удаления данных о документе в базе и файла документа в хранилище.");
        User user = (User) userService.getAuthenticatedUser();
        Document document =
                docUserAccessService.getCheckedDocumentForOperations(AccessType.REMOVE, docId, user.getUsername());
        fileManager.deleteFile(Paths.get(document.getFilePath()));
        documentRepository.deleteById(docId);
        document.setDescription("REMOVED");
        DocumentDto documentDto = DocumentMapper.toDto(document);
        log.debug("Данные о документе удалены из базы, а файл документа в хранилище.");
        return documentDto;
    }

    private Document getCheckedForOwnerDocument(Long docId, String username) {
        Document document = documentRepository.findById(docId)
                .orElseThrow(() -> new ObjectNotFoundException(docId, "Document"));
        if (!username.equals(document.getOwner().getUsername())) {
            throw new IllegalArgumentException(String.format("Пользователь '%s' не является обладателем " +
                    "документа с id=%d и не может управлять доступом к нему.", username, docId));
        }
        return document;
    }

    private Pageable makePageableWithParameters(Object paramHolder) {
        Pageable page = null;
        if (paramHolder instanceof GetDocsParamHolder get) {
            page = PageRequest.of((get.from() / get.size()),
                    get.size(),
                    get.order(),
                    get.sortBy().getPropertyName()
            );
        }
        if (paramHolder instanceof SearchInDocsParamHolder search) {
            page = PageRequest.of((search.from() / search.size()),
                    search.size(),
                    search.order(),
                    search.sortBy().getPropertyName()
            );
        }
        return page;
    }

}
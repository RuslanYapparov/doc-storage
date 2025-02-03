package ru.yappy.docstorage.service.mapper;

import lombok.experimental.UtilityClass;
import ru.yappy.docstorage.model.Document;
import ru.yappy.docstorage.model.dto.*;

import java.nio.file.FileSystems;
import java.util.stream.Stream;

@UtilityClass
public class DocumentMapper {
    private static final String SEPARATOR = FileSystems.getDefault().getSeparator();

    public DocumentDto toDto(Document document) {
        DocUserAccessDto[] usersWithAccessDtos = (document.getUsersWithAccess() != null) ?
                DocUserAccessMapper.toDtoArray(document.getUsersWithAccess()) : null;
        return new DocumentDto(
                document.getId(),
                document.getTitle(),
                document.getDescription(),
                document.getOwner().getUsername(),
                getFilenameFromPath(document.getFilePath()),
                document.getCreatedAt(),
                document.getCommonAccessType(),
                document.getUpdatedBy(),
                document.getUpdatedAt(),
                usersWithAccessDtos
        );
    }

    public DocumentDto[] toDtoArray(Stream<Document> documents) {
        return documents
                .map(DocumentMapper::toDto)
                .toArray(DocumentDto[]::new);
    }

    private String getFilenameFromPath(String filePath) {
        return filePath.substring(filePath.lastIndexOf(SEPARATOR) + 1);
    }

}
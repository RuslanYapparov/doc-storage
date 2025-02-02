package ru.yappy.docstorage.service.mapper;

import lombok.experimental.UtilityClass;
import ru.yappy.docstorage.model.Document;
import ru.yappy.docstorage.model.dto.*;

@UtilityClass
public class DocumentMapper {

    public DocumentDto toDto(Document document) {
        DocUserAccessDto[] usersWithAccessDtos = (document.getUsersWithAccess() != null) ?
                DocUserAccessMapper.toDtoArray(document.getUsersWithAccess()) : null;
        return new DocumentDto(
                document.getId(),
                document.getTitle(),
                document.getDescription(),
                document.getOwner().getUsername(),
                document.getFilePath(),
                document.getCreatedAt(),
                document.isSharedForAll(),
                document.getAccessTypeForAll(),
                usersWithAccessDtos
        );
    }

}
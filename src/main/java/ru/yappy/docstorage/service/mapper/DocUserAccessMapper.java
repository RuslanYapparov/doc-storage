package ru.yappy.docstorage.service.mapper;

import lombok.experimental.UtilityClass;
import ru.yappy.docstorage.model.DocUserAccess;
import ru.yappy.docstorage.model.dto.DocUserAccessDto;

import java.util.Set;

@UtilityClass
public class DocUserAccessMapper {

    public DocUserAccessDto toDto(DocUserAccess docUserAccess) {
        return new DocUserAccessDto(
                docUserAccess.getDocId(),
                docUserAccess.getUsername(),
                docUserAccess.getAccessType()
        );
    }

    public DocUserAccess toModel(DocUserAccessDto docUserAccessDto) {
        return new DocUserAccess(
                docUserAccessDto.docId(),
                docUserAccessDto.username(),
                docUserAccessDto.accessType()
        );
    }

    public DocUserAccessDto[] toDtoArray(Set<DocUserAccess> docUserAccesses) {
        return docUserAccesses.stream()
                .map(DocUserAccessMapper::toDto)
                .toArray(DocUserAccessDto[]::new);
    }

}
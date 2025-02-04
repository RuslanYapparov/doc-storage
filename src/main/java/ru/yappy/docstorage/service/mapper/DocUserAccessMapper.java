package ru.yappy.docstorage.service.mapper;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.LazyInitializationException;
import ru.yappy.docstorage.model.DocUserAccess;
import ru.yappy.docstorage.model.dto.DocUserAccessDto;

import java.util.Set;

@Slf4j
@UtilityClass
public class DocUserAccessMapper {

    public DocUserAccessDto toDto(DocUserAccess docUserAccess) {
        return new DocUserAccessDto(
                docUserAccess.getDocId(),
                docUserAccess.getUsername(),
                docUserAccess.getAccessType()
        );
    }

    public DocUserAccessDto[] toDtoArray(Set<DocUserAccess> docUserAccesses) {
        try {
            return docUserAccesses.stream()
                    .map(DocUserAccessMapper::toDto)
                    .toArray(DocUserAccessDto[]::new);
        } catch (LazyInitializationException exception) {
            log.info("Для мэппинга сущностей DocUserAccess поступили прокси-объекты, " +
                    "которые не могут быть загружены из БД (сессия закрыта). Будет возвращен пустой массив.");
            return new DocUserAccessDto[0];
        }
    }

}
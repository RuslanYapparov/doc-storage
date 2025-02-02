package ru.yappy.docstorage.model.dto;

import ru.yappy.docstorage.model.AccessType;

public record DocUserAccessDto(Long docId,
                               String username,
                               AccessType accessType) {}
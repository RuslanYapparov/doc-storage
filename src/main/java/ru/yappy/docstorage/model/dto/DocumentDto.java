package ru.yappy.docstorage.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import ru.yappy.docstorage.model.AccessType;

import java.time.LocalDate;

public record DocumentDto(Long id,
                          String title,
                          String description,
                          String ownerName,
                          String fileName,
                          @JsonFormat(pattern = "dd-MM-yyyy")
                          LocalDate createdAt,
                          Boolean isSharedForAll,
                          AccessType accessTypeForAll,
                          DocUserAccessDto[] usersWithAccess) {}
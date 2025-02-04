package ru.yappy.docstorage.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import ru.yappy.docstorage.model.AccessType;

import java.time.*;

public record DocumentDto(Long id,
                          String title,
                          String description,
                          String ownerName,
                          String fileName,
                          @JsonFormat(pattern = "dd-MM-yyyy")
                          LocalDate createdAt,
                          AccessType commonAccessType,
                          String updatedBy,
                          @JsonFormat(pattern = "dd-MM-yyyy HH:mm:ss")
                          LocalDateTime updatedAt) {}
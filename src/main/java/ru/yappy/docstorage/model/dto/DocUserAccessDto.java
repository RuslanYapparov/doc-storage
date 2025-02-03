package ru.yappy.docstorage.model.dto;

import jakarta.validation.constraints.*;
import ru.yappy.docstorage.model.AccessType;

public record DocUserAccessDto(@NotNull @Min(1) Long docId,
                               @NotBlank String username,
                               @NotNull AccessType accessType) {}
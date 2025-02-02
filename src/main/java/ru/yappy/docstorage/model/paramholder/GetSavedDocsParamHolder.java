package ru.yappy.docstorage.model.paramholder;

import org.springframework.data.domain.Sort;

public record GetSavedDocsParamHolder(DocSortType sortBy,
                                      Sort.Direction order,
                                      int from,
                                      int size) {}
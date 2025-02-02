package ru.yappy.docstorage.model.paramholder;

import lombok.Getter;

@Getter
public enum DocSortType {
    DATE("createdAt"),
    TITLE("title"),
    DESCRIPTION("description"),
    OWNER("owner.username");

    private final String propertyName;

    DocSortType(String propertyName) {
        this.propertyName = propertyName;
    }

}
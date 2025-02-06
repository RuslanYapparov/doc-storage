package ru.yappy.docstorage.model.paramholder;

import org.springframework.data.domain.Sort;

import java.time.LocalDate;

public record SearchInDocsParamHolder(String searchFor,
                                      LocalDate since,
                                      LocalDate until,
                                      DocSortType sortBy,
                                      Sort.Direction order,
                                      int from,
                                      int size,
                                      boolean withOwned,
                                      boolean withSharedForAll) {

    public SearchInDocsParamHolder(String searchFor,
                                   LocalDate since,
                                   LocalDate until,
                                   DocSortType sortBy,
                                   Sort.Direction order,
                                   int from,
                                   int size) {
        this(searchFor, since, until, sortBy, order, from, size, true, true);
    }

    public String toStringForSavedDocs() {
        return "SearchInDocsParamHolder{" +
                "searchFor='" + searchFor + '\'' +
                ", since=" + since +
                ", until=" + until +
                ", sortBy=" + sortBy +
                ", order=" + order +
                ", from=" + from +
                ", size=" + size +
                '}';
    }

}
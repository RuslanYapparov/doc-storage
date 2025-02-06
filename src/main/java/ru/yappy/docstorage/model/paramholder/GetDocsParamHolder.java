package ru.yappy.docstorage.model.paramholder;

import org.springframework.data.domain.Sort;

public record GetDocsParamHolder(DocSortType sortBy,
                                 Sort.Direction order,
                                 int from,
                                 int size,
                                 boolean withOwned,
                                 boolean withSharedForAll) {

    public GetDocsParamHolder(DocSortType sortBy,
                              Sort.Direction order,
                              int from,
                              int size) {
        this(sortBy, order, from, size, true, true);
    }

    public String toStringForSavedDocs() {
        return "GetDocsParamHolder{" +
                "sortBy=" + sortBy +
                ", order=" + order +
                ", from=" + from +
                ", size=" + size +
                '}';
    }

}
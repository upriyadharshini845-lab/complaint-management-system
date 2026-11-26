package com.cms.complaint.service;

import org.springframework.data.domain.Sort;

import java.util.List;

/** Safe defaults for page, size and sort so clients cannot ask for unlimited rows or sort by any column. */
public final class PagingUtil {

    public static final int DEFAULT_SIZE = 10;
    public static final int MAX_SIZE = 50;
    public static final List<String> SORT_FIELDS = List.of("createdAt", "updatedAt", "deadline", "title", "trackingId");

    private PagingUtil() {
    }

    public static int size(Integer size) {
        if (size == null || size < 1) {
            return DEFAULT_SIZE;
        }
        return Math.min(size, MAX_SIZE);
    }

    public static int page(Integer page) {
        return page == null || page < 0 ? 0 : page;
    }

    public static String sortField(String field) {
        return field != null && SORT_FIELDS.contains(field) ? field : "createdAt";
    }

    public static Sort.Direction direction(String dir) {
        return "asc".equalsIgnoreCase(dir) ? Sort.Direction.ASC : Sort.Direction.DESC;
    }
}

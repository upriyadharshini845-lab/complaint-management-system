package com.cms.complaint.service;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PagingUtilTest {

    @Test
    void sizeIsClampedToSafeRange() {
        assertEquals(10, PagingUtil.size(null));
        assertEquals(10, PagingUtil.size(0));
        assertEquals(5, PagingUtil.size(5));
        assertEquals(50, PagingUtil.size(1000));
    }

    @Test
    void pageIsNeverNegative() {
        assertEquals(0, PagingUtil.page(null));
        assertEquals(0, PagingUtil.page(-3));
        assertEquals(4, PagingUtil.page(4));
    }

    @Test
    void sortFieldMustBeWhitelisted() {
        assertEquals("title", PagingUtil.sortField("title"));
        assertEquals("createdAt", PagingUtil.sortField("password"));
        assertEquals("createdAt", PagingUtil.sortField(null));
    }

    @Test
    void directionDefaultsToDescending() {
        assertEquals(Sort.Direction.ASC, PagingUtil.direction("ASC"));
        assertEquals(Sort.Direction.DESC, PagingUtil.direction("desc"));
        assertEquals(Sort.Direction.DESC, PagingUtil.direction(null));
    }
}

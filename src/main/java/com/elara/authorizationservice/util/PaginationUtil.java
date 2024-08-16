package com.elara.authorizationservice.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

public class PaginationUtil {

    public static PageRequest getPageRequest(int pageIndex, int pageSize) {
        pageIndex = Math.max(pageIndex, 0);
        pageSize = Math.max(pageSize, 0);
        return PageRequest.of(pageIndex, pageSize, Sort.Direction.DESC, "createdAt");
    }

    public static PageRequest getPageRequest(int pageIndex, int pageSize, String sortBy) {
        pageIndex = Math.max(pageIndex, 0);
        pageSize = Math.max(pageSize, 0);
        return PageRequest.of(pageIndex, pageSize, Sort.Direction.DESC, "createdAt");
    }

}
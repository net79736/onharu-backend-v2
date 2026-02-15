package com.backend.onharu.interfaces.api.common.util;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.JpaSort;

/**
 * Pageable 객체 생성 유틸리티
 * 
 * 다양한 방식으로 페이징 객체를 생성합니다.
 * 
 * ## 기본 사용
 * Pageable pageable = PageableUtil.of(page, size, sort);
 * 
 * ## 정렬 직접 지정
 * Pageable pageable = PageableUtil.of(page, size, "createdAt", "desc");
 * 
 * ## 기본값 사용
 * Pageable pageable = PageableUtil.ofDefault();
 */
public class PageableUtil {

    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_SIZE = 10;
    private static final String DEFAULT_SORT_FIELD = "id";

    public static Pageable ofOneBased(
        Integer pageNum, 
        Integer perPage,
        String sortField, 
        String sortDirection) {
    
        int validPageNum = (pageNum != null && pageNum > 0) ? pageNum : DEFAULT_PAGE;
        int validPerPage = (perPage != null && perPage > 0) ? perPage : DEFAULT_SIZE;
        int zeroBasedPage = validPageNum - 1;

        return of(zeroBasedPage, validPerPage, sortField, sortDirection);
    }

    public static Pageable of(
            int page, 
            int size, 
            String sortField, 
            String sortDirection) {
        
        int validPage = Math.max(0, page);
        int validSize = size > 0 ? size : 10;
        String validField = (sortField == null || sortField.isEmpty()) ? DEFAULT_SORT_FIELD : sortField;

        Sort.Direction direction = "desc".equalsIgnoreCase(sortDirection)
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        Sort sort = createSort(validField, direction);
        return PageRequest.of(validPage, validSize, sort);
    }

    private static Sort createSort(String field, Sort.Direction direction) {
        // Native Query의 계산식이나 alias는 unsafe 사용
        // 괄호로 감싸진 표현식이나 함수 호출
        if (field.contains("(") && field.contains(")")) {
            return JpaSort.unsafe(direction, field);
        }
        
        return Sort.by(direction, field);
    }

    public static int getCurrentPage(Page<?> page) {
        return page.getNumber() + 1;
    }
}
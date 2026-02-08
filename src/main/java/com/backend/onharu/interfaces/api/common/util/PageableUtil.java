package com.backend.onharu.interfaces.api.common.util;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

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

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 10;
    private static final String DEFAULT_SORT_FIELD = "id";

    /**
     * Pageable 생성 (정렬 없음)
     * 
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @return Pageable 객체
     */
    public static Pageable of(int page, int size) {
        int validPage = Math.max(DEFAULT_PAGE, page);
        int validSize = size > 0 ? size : DEFAULT_SIZE;
        return PageRequest.of(validPage, validSize);
    }

    /**
     * 1-based 페이지 번호로 Pageable 생성
     * 
     * 프론트엔드에서 1부터 시작하는 페이지 번호를 사용할 때
     * 
     * @param pageNum 페이지 번호 (1부터 시작)
     * @param perPage 페이지당 항목 수
     * @param sortField 정렬 필드명
     * @param sortDirection 정렬 방향
     * @return Pageable 객체
     */
    public static Pageable ofOneBased(Integer pageNum, Integer perPage, 
                                       String sortField, String sortDirection) {
        int validPageNum = (pageNum != null && pageNum > 0) ? pageNum : 1;
        int validPerPage = (perPage != null && perPage > 0) ? perPage : DEFAULT_SIZE;
        
        // 1-based → 0-based 변환
        int zeroBasedPage = validPageNum - 1;
        
        return of(zeroBasedPage, validPerPage, sortField, sortDirection);
    }

    /**
     * Pageable 생성 (필드와 방향 직접 지정)
     * 
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @param sortField 정렬 필드명
     * @param sortDirection 정렬 방향 ("asc" | "desc")
     * @return Pageable 객체
     */
    public static Pageable of(int page, int size, String sortField, String sortDirection) {
        int validPage = Math.max(DEFAULT_PAGE, page);
        int validSize = size > 0 ? size : DEFAULT_SIZE;
        String validField = (sortField == null || sortField.isEmpty()) ? DEFAULT_SORT_FIELD : sortField; // 정렬 필드명이 없으면 id로 기본 정렬
        
        Sort.Direction direction = "desc".equalsIgnoreCase(sortDirection) // 정렬 방향이 없으면 asc로 기본 정렬
                ? Sort.Direction.DESC 
                : Sort.Direction.ASC;
        
        return PageRequest.of(validPage, validSize, Sort.by(direction, validField));
    }

    /**
     * Page 객체에서 1-based 현재 페이지 번호를 반환
     * 
     * Spring Data의 Page.getNumber()는 0-based 인덱스를 반환하므로,
     * 프론트엔드에서 사용하기 편하도록 1-based로 변환합니다.
     * 
     * @param page Page 객체
     * @return 1-based 현재 페이지 번호 (1부터 시작)
     */
    public static int getCurrentPage(Page<?> page) {
        return page.getNumber() + 1;
    }
}
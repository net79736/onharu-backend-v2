package com.backend.onharu.infra.nts;

public interface NtsBusinessNumber {

    /**
     * 사업자 등록번호가 유효하지 확인
     *
     * @param businessNumber 사업자 등록번호
     * @return true: 유효함, false: 유효하지 않음
     */
    boolean isValid(String businessNumber);
}

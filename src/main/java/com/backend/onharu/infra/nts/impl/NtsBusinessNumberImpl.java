package com.backend.onharu.infra.nts.impl;

import com.backend.onharu.domain.support.error.CoreException;
import com.backend.onharu.infra.nts.NtsBusinessNumber;
import com.backend.onharu.infra.nts.NtsResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

import static com.backend.onharu.domain.support.error.ErrorType.Owner.BUSINESS_NUMBER_MUST_BE_TEN_DIGITS;

/**
 * 국세청의 사업자 등록번호 진위확인 API 의 호출을 담당하는 코드 입니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NtsBusinessNumberImpl implements NtsBusinessNumber {

    private static final String CONTINUING_BUSINESS = "계속사업자"; // 국세청 API 응답

    private final RestTemplate restTemplate; // 외부 API 호출용 RestTemplate

    @Value("${external.nts.base-url}")
    private String baseUrl;

    @Value("${external.nts.serviceKey}")
    private String serviceKey;

    /**
     * 사업자 등록번호가 유효하지 확인
     *
     * @param businessNumber 사업자 등록번호
     * @return 정상 영업 중인 사업자(계속사업자)인 경우 true, 휴/폐업 및 미등록 번호인 경우 false
     * @throws CoreException 사업자 번호 형식이 10자리 숫자가 아닐 경우 예외 발생
     */
    @Override
    public boolean isValid(String businessNumber) {
        // 입력받은 사업자 등록번호 검증
        if (!businessNumber.matches("\\d{10}")) {
            throw new CoreException(BUSINESS_NUMBER_MUST_BE_TEN_DIGITS);
        }
        // API 요청 URL 생성
        String url = UriComponentsBuilder
                .fromHttpUrl(baseUrl)
                .queryParam("serviceKey", serviceKey)
                .toUriString();

        // HTTP 헤더
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        // HTTP 바디
        Map<String, Object> body = Map.of(
                "b_no", new String[]{businessNumber}
        );
        // HTTP 헤더와 바디를 결합하여 전송할 요청 엔티티
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            // 국세청 API 로 POST 요청 및 수신
            ResponseEntity<NtsResponse> response = restTemplate.postForEntity(url, request, NtsResponse.class);
            NtsResponse ntsResponse = response.getBody();

            // 응답이 null 이거나 비어있는 경우
            if (ntsResponse == null || ntsResponse.data().isEmpty()) {
                log.error("국세청 API 응답이 비어있음");
                return false; // 응답이 비어있는 경우 false 반환
            }

            // 사업자 상태값 추출
            String status = ntsResponse.data().get(0).businessStatus();

            return CONTINUING_BUSINESS.equals(status); // 사업자 상태값이 "계속사업자"와 일치하는지 확인

        } catch (Exception e) {
            log.error("국세청 API 호출 중 에러 발생: {}", e.getMessage());
            return false; // API 호출 중 오류가 발생할 경우 false 반환
        }
    }
}

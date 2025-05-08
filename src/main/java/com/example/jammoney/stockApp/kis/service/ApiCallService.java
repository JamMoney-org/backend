package com.example.jammoney.stockApp.kis.service;
import com.example.jammoney.stockApp.kis.dto.StockAskingPriceDto;
import com.example.jammoney.stockApp.kis.dto.StockMinDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApiCallService {

    private final KisAuthService kisAuthService;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${kis.app.key}")
    private String appKey;

    @Value("${kis.app.secret}")
    private String appSecret;

    @Value("${kis.base-url}")
    private String baseUrl;

    @Value("${kis.url.stockasbi}")
    private String stockAsbiUrl;

    @Value("${kis.url.stockmin}")
    private String stockMinUrl;

    @Value("${kis.url.price}")
    private String stockPriceUrl;

    @Value("${kis.url.kospi}")
    private String kospiUrl;

    /**
     * 실시간 시세 호출 (현재가)
     */
    public Object getCurrentPrice(String stockCode) {
        HttpHeaders headers = createHeaders("FHKST01010100");
        String uri = UriComponentsBuilder.fromHttpUrl(baseUrl + stockPriceUrl)
                .queryParam("fid_cond_mrkt_div_code", "J")  // KOSPI
                .queryParam("fid_input_iscd", stockCode)
                .toUriString();

        return sendGet(uri, headers, Object.class);
    }

    /**
     * 10단계 호가/잔량 데이터
     */
    public StockAskingPriceDto getStockAsbi(String stockCode) {
        HttpHeaders headers = createHeaders("FHKST01010200");
        String uri = UriComponentsBuilder.fromHttpUrl(baseUrl + stockAsbiUrl)
                .queryParam("FID_COND_MRKT_DIV_CODE", "J")
                .queryParam("FID_INPUT_ISCD", stockCode)
                .toUriString();

        return sendGet(uri, headers, StockAskingPriceDto.class);
    }

    /**
     * 1분봉 (시간대별 시세 데이터)
     */
    public StockMinDto getStockMin(String stockCode, String time) {
        HttpHeaders headers = createHeaders("FHKST03010200");
        String uri = UriComponentsBuilder.fromHttpUrl(baseUrl + stockMinUrl)
                .queryParam("FID_COND_MRKT_DIV_CODE", "J")
                .queryParam("FID_INPUT_ISCD", stockCode)
                .queryParam("FID_ETC_CLS_CODE", "")
                .queryParam("FID_INPUT_HOUR_1", time)
                .queryParam("FID_PW_DATA_INCU_YN", "Y")
                .toUriString();

        return sendGet(uri, headers, StockMinDto.class);
    }

    /**
     * 공통 GET 요청 처리
     */
    private <T> T sendGet(String uri, HttpHeaders headers, Class<T> clazz) {
        HttpEntity<String> entity = new HttpEntity<>("parameters", headers);
        try {
            ResponseEntity<T> response = restTemplate.exchange(
                    uri,
                    HttpMethod.GET,
                    entity,
                    clazz
            );
            return response.getBody();
        } catch (Exception e) {
            log.error("API 요청 실패: {}", e.getMessage());
            return null;
        }
    }

    private HttpHeaders createHeaders(String trId) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + kisAuthService.getAccessToken());
        headers.set("appkey", appKey);
        headers.set("appsecret", appSecret);
        headers.set("tr_id", trId);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }
}



package com.example.jammoney.stockApp.kis.service;
import com.example.jammoney.stockApp.kis.dto.KospiDto;
import com.example.jammoney.stockApp.kis.dto.StockAskingPriceDto;
import com.example.jammoney.stockApp.kis.dto.StockMinDto;
import com.example.jammoney.stockApp.kis.dto.StockPriceDto;
import com.example.jammoney.stockApp.stock.dto.KospiResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

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

    @Value("${kis.url.stockAskingPrice}")
    private String stockAskingPriceUrl;

    @Value("${kis.url.stockmin}")
    private String stockMinUrl;

    @Value("${kis.url.price}")
    private String stockPriceUrl;

    @Value("${kis.url.kospi}")
    private String kospiUrl;

    /**
     * 실시간 시세 호출 (현재가)
     */
    public long getCurrentPrice(String stockCode) {
        HttpHeaders headers = createHeaders("FHKST01010100");
        String uri = UriComponentsBuilder.fromHttpUrl(baseUrl + stockPriceUrl)
                .queryParam("fid_cond_mrkt_div_code", "J")  // 시장 구분 (KOSPI 등)
                .queryParam("fid_input_iscd", stockCode)
                .toUriString();

        StockPriceDto response = sendGet(uri, headers, StockPriceDto.class);
        String priceStr = Objects.requireNonNull(response).getOutput().getStck_prpr();

        if (priceStr == null || priceStr.isBlank()) {
            throw new RuntimeException("현재가 정보가 비어 있습니다.");
        }

        try {
            return Long.parseLong(priceStr);
        } catch (NumberFormatException e) {
            throw new RuntimeException("현재가 형식이 잘못되었습니다: " + priceStr);
        }
    }

    /**
     * 10단계 호가/잔량 데이터
     */
    public StockAskingPriceDto getStockAskingPrice(String stockCode) {
        HttpHeaders headers = createHeaders("FHKST01010200");
        String uri = UriComponentsBuilder.fromHttpUrl(baseUrl + stockAskingPriceUrl)
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

    public List<KospiResponseDto> getKospiMonthlyIndexThisYear() {
        HttpHeaders headers = createHeaders("FHKUP03500100");


        String startDate = "20241231";
        String endDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        String uri = baseUrl + kospiUrl+"?FID_COND_MRKT_DIV_CODE=U&FID_INPUT_ISCD=" + "0001" + "&FID_INPUT_DATE_1=" + startDate
                +"&FID_INPUT_DATE_2=" + endDate + "&FID_PERIOD_DIV_CODE=" + "M";

        KospiDto response = sendGet(uri, headers, KospiDto.class);
        if (response == null || response.getOutput2() == null) {
            log.warn("응답이 null 또는 output2가 없습니다.");
            return Collections.emptyList();
        }
        log.info("output2 size: {}", response.getOutput2().size());
        List<KospiResponseDto> result = new ArrayList<>();
        for (KospiDto.KospiRawItem item : response.getOutput2()) {
            String yyyymm = item.getStck_bsop_date().substring(0, 6);
            if (yyyymm.compareTo(startDate) >= 0 && yyyymm.compareTo(endDate) <= 0)  {
                KospiResponseDto dto = new KospiResponseDto();
                dto.setDate(yyyymm);
                dto.setOpen(parseDouble(item.getBstp_nmix_oprc()));
                dto.setHigh(parseDouble(item.getBstp_nmix_hgpr()));
                dto.setLow(parseDouble(item.getBstp_nmix_lwpr()));
                dto.setClose(parseDouble(item.getBstp_nmix_prpr()));
                log.info("raw response = {}", dto);
                result.add(dto);
            }
        }

        // 날짜 오름차순 정렬
        result.sort(Comparator.comparing(KospiResponseDto::getDate));
        return result;
    }

    private double parseDouble(String s) {
        try {
            return Double.parseDouble(s);
        } catch (Exception e) {
            return 0.0;
        }
    }
    public KospiDto getKospiData(String fromDate, String toDate, String period) {
        HttpHeaders headers = createHeaders("FHKUP03500100");

        String uri = baseUrl + kospiUrl +
                "?FID_COND_MRKT_DIV_CODE=U" +
                "&FID_INPUT_ISCD=0001" +
                "&FID_INPUT_DATE_1=" + fromDate +
                "&FID_INPUT_DATE_2=" + toDate +
                "&FID_PERIOD_DIV_CODE=" + period;

        return sendGet(uri, headers, KospiDto.class);
    }


    /**
     * 공통 GET 요청 처리
     */
    private <T> T sendGet(String uri, HttpHeaders headers, Class<T> clazz) {
        HttpEntity<String> entity = new HttpEntity<>("parameters", headers);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
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



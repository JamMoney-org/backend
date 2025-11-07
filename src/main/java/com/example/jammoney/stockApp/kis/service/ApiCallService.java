package com.example.jammoney.stockApp.kis.service;

import com.example.jammoney.stockApp.kis.dto.*;
import com.example.jammoney.stockApp.stock.dto.KospiResponseDto;
import com.example.jammoney.stockApp.stock.dto.StockMetaDataResponseDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApiCallService {

    private final KisAuthService kisAuthService;
    private final RestTemplate restTemplate; // 이름 매칭

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
    private String stockMetaDataUrl;

    @Value("${kis.url.kospi}")
    private String kospiUrl;

    /**
     * 주식 메타 데이터
     */
    public StockMetaDataResponseDto getStockMetaData(String stockCode) {
        HttpHeaders headers = createHeaders("FHKST01010100");
        String uri = UriComponentsBuilder.fromHttpUrl(baseUrl + stockMetaDataUrl)
                .queryParam("fid_cond_mrkt_div_code", "J")
                .queryParam("fid_input_iscd", stockCode)
                .toUriString();

        StockMetaDataDto response = sendGet(uri, headers, StockMetaDataDto.class);

        if (response == null || response.getOutput() == null) {
            throw new RuntimeException("KIS API 응답이 비어 있습니다.");
        }

        StockMetaDataDto.Output output = response.getOutput();

        StockMetaDataResponseDto dto = new StockMetaDataResponseDto();
        dto.setHts_avls(output.getHts_avls());
        dto.setStck_fcam(output.getStck_fcam());
        dto.setLstn_stcn(output.getLstn_stcn());
        dto.setBstp_kor_isnm(output.getBstp_kor_isnm());
        dto.setEps(output.getEps());
        dto.setPer(output.getPer());
        dto.setBps(output.getBps());
        dto.setPbr(output.getPbr());
        dto.setStac_month(output.getStac_month());

        return dto;
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
                .queryParam("FID_PW_DATA_INCU_YN", "N")
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
    private final ObjectMapper objectMapper = new ObjectMapper();

    private <T> T sendGet(String uri, HttpHeaders headers, Class<T> clazz) {
        // GET: Content-Type 제거 (일부 WAF/프록시가 오동작)
        headers.remove(HttpHeaders.CONTENT_TYPE);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.set(HttpHeaders.USER_AGENT, "JamMoneyBot/1.0");

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            // 1) RAW로 받기
            ResponseEntity<byte[]> res = restTemplate.exchange(uri, HttpMethod.GET, entity, byte[].class);

            int code = res.getStatusCode().value();
            MediaType ct = res.getHeaders().getContentType();
            byte[] body = res.getBody() == null ? new byte[0] : res.getBody();
            String peek = new String(body, StandardCharsets.UTF_8);

            log.info("[KIS RAW] status={} ct={} len={} peek={}",
                    code, ct, body.length, peek.substring(0, Math.min(peek.length(), 500)).replace("\n"," "));

            // 2) JSON 아닌 응답은 즉시 예외(HTML 등)
            if (ct == null || !MediaType.APPLICATION_JSON.isCompatibleWith(ct)) {
                throw new IllegalStateException("KIS 응답이 JSON이 아님: status=" + code
                        + ", ct=" + ct + ", bodyPeek=" + peek.substring(0, Math.min(peek.length(), 500)));
            }

            // 3) JSON -> DTO
            return objectMapper.readValue(body, clazz);

        } catch (org.springframework.web.client.RestClientResponseException e) {
            // 4xx/5xx인 경우
            log.error("KIS 실패 status={}, contentType={}, headers={}",
                    e.getRawStatusCode(),
                    e.getResponseHeaders() != null ? e.getResponseHeaders().getFirst(HttpHeaders.CONTENT_TYPE) : null,
                    e.getResponseHeaders());
            String b = e.getResponseBodyAsString();
            if (b != null) log.error("KIS 실패 body(first 1000): {}", b.length() > 1000 ? b.substring(0, 1000) : b);
            throw e;
        } catch (org.springframework.web.client.UnknownContentTypeException e) {
            // 혹시라도 또 오면 스택 남김
            log.error("KIS UnknownContentType: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("KIS 호출 중 예외: {}", e.toString(), e);
            throw new RuntimeException(e);
        }
    }

    private HttpHeaders createHeaders(String trId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(kisAuthService.getAccessToken());
        headers.set("appkey", appKey);
        headers.set("appsecret", appSecret);
        headers.set("tr_id", trId);
        headers.set("custtype", "P");
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        return headers;
    }
}



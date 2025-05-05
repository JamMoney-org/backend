package com.example.Jammoney.StockApp.kis.api;

import com.example.Jammoney.StockApp.kis.auth.KisAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class KisStockService {

    private final KisAuthService kisAuthService;

    @Value("${kis.app.key}")
    private String appKey;

    @Value("${kis.app.secret}")
    private String appSecret;

    @Value("${kis.base-url}")
    private String baseUrl;

    public Map<String, Object> getCurrentPrice(String stockCode) {
        String url = baseUrl + "/uapi/domestic-stock/v1/quotations/inquire-price";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + kisAuthService.getAccessToken());
        headers.set("appkey", appKey);
        headers.set("appsecret", appSecret); // 일부 API는 secret 재사용


        headers.set("tr_id", "FHKST01010100"); // 모의투자용 TR
        headers.setContentType(MediaType.APPLICATION_JSON);

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url)
                .queryParam("fid_cond_mrkt_div_code", "J") // KOSPI: J, KOSDAQ: Q
                .queryParam("fid_input_iscd", stockCode);  // ex: "005930"

        HttpEntity<Void> entity = new HttpEntity<>(headers);
        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<Map> response = restTemplate.exchange(
                builder.toUriString(),
                HttpMethod.GET,
                entity,
                Map.class
        );

        return response.getBody();
    }
}


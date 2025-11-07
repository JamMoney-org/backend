package com.example.jammoney.config;

import org.apache.hc.client5.http.ConnectionKeepAliveStrategy;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate() {
        var cm = PoolingHttpClientConnectionManagerBuilder.create()
                .setMaxConnTotal(50)
                .setMaxConnPerRoute(10)
                .build();

        var requestConfig = RequestConfig.custom()
                .setConnectTimeout(Timeout.ofSeconds(3))   // 연결 타임아웃
                .setResponseTimeout(Timeout.ofSeconds(5))  // 응답 타임아웃
                .build();

        ConnectionKeepAliveStrategy keepAlive = (res, ctx) -> TimeValue.ofSeconds(15);

        HttpClient httpClient = HttpClients.custom()
                .setConnectionManager(cm)
                .setDefaultRequestConfig(requestConfig)
                .setKeepAliveStrategy(keepAlive)
                .build();

        var factory = new HttpComponentsClientHttpRequestFactory(httpClient);
        var rt = new RestTemplate(factory);

        // 공통 인터셉터: UA/Accept 헤더 기본 부착
        rt.getInterceptors().add((req, body, exec) -> {
            req.getHeaders().set(
                    "User-Agent",
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
                            "AppleWebKit/537.36 (KHTML, like Gecko) " +
                            "Chrome/120.0.0.0 Safari/537.36"
            );
            // 스펙에 맞춰 JSON 선호
            req.getHeaders().setAccept(List.of(MediaType.APPLICATION_JSON));
            return exec.execute(req, body);
        });

        return rt;
    }
}

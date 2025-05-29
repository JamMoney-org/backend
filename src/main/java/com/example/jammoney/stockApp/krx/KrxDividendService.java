package com.example.jammoney.stockApp.krx;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class KrxDividendService {

    private final RestTemplate restTemplate = new RestTemplate();

    public List<DividendData> fetchDividendData() {
        String otp = generateOtp();
        byte[] excelData = downloadExcel(otp);
        return parseExcel(excelData);
    }

    private String generateOtp() {
        String url = "https://data.krx.co.kr/comm/fileDn/GenerateOTP/generate.cmd";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("locale", "ko_KR");
        params.add("mktId", "ALL");
        params.add("trdDd", LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
        params.add("share", "1");
        params.add("money", "1");
        params.add("csvxls_isNo", "false");
        params.add("name", "fileDown");
        params.add("url", "dbms/MDC/STAT/standard/MDCSTAT03501");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
        return response.getBody();
    }

    private byte[] downloadExcel(String otp) {
        String url = "https://data.krx.co.kr/comm/fileDn/download_excel/download.cmd";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("code", otp);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        ResponseEntity<byte[]> response = restTemplate.postForEntity(url, request, byte[].class);
        return response.getBody();
    }

    private List<DividendData> parseExcel(byte[] excelData) {
        List<DividendData> dividendDataList = new ArrayList<>();
        try (InputStream is = new ByteArrayInputStream(excelData);
             Workbook workbook = WorkbookFactory.create(is)) {
            Sheet sheet = workbook.getSheetAt(0);
            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // Skip header

                String code = getCellValue(row.getCell(0));
                String dividendYieldStr = getCellValue(row.getCell(12));
                String dividendPerShareStr = getCellValue(row.getCell(11));

                BigDecimal dividendYield = parseBigDecimal(dividendYieldStr);
                BigDecimal dividendPerShare = parseBigDecimal(dividendPerShareStr);

                dividendDataList.add(new DividendData(code, dividendYield, dividendPerShare));
            }
        } catch (Exception e) {
            log.error("Excel parsing failed", e);
        }
        return dividendDataList;
    }

    private String getCellValue(Cell cell) {
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf(cell.getNumericCellValue());
            default -> "";
        };
    }

    private BigDecimal parseBigDecimal(String s) {
        try {
            return (s != null && !s.isBlank()) ? new BigDecimal(s.replace(",", "")) : null;
        } catch (Exception e) {
            return null;
        }
    }

    public record DividendData(String code, BigDecimal dividendYield, BigDecimal dividendPerShare) {}
}

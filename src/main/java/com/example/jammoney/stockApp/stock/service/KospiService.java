package com.example.jammoney.stockApp.stock.service;

import com.example.jammoney.stockApp.kis.dto.KospiDto;
import com.example.jammoney.stockApp.kis.entity.Kospi;
import com.example.jammoney.stockApp.kis.entity.KospiRepository;
import com.example.jammoney.stockApp.kis.service.ApiCallService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class KospiService {

    private final ApiCallService apiCallService;
    private final KospiRepository kospiRepository;

    public List<Kospi> saveMonthlyKospiIndex() {
        List<Kospi> kospis = new ArrayList<>();
        String fromDate = "20241231";
        String toDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")); // 오늘

        KospiDto response = apiCallService.getKospiData(fromDate, toDate, "M");  // M: 월봉
        if (response == null || response.getOutput2() == null) {
            log.warn("KOSPI 월별 응답이 비었습니다.");
            return null;
        }

        List<KospiDto.KospiRawItem> items = response.getOutput2();
        for (KospiDto.KospiRawItem item : items) {
            try {
                String yyyymm = item.getStck_bsop_date().substring(0, 6);
                LocalDate date = LocalDate.parse(yyyymm + "01", DateTimeFormatter.ofPattern("yyyyMMdd"));

                if (kospiRepository.findByDate(date).isPresent()) {
                    log.info("{} 이미 존재하여 저장하지 않음", date);
                    continue;
                }

                Kospi kospi = new Kospi();
                kospi.setDate(date);
                kospi.setClose(Double.parseDouble(item.getBstp_nmix_prpr()));
                kospi.setPrdyVrss(Double.parseDouble(item.getBstp_nmix_oprc()) - Double.parseDouble(item.getBstp_nmix_prpr()));
                kospi.setPrdyCrt(parsePercent(item.getBstp_nmix_prpr(), item.getBstp_nmix_oprc()));

                kospiRepository.save(kospi);
                kospis.add(kospi);
                log.info("KOSPI 저장 완료: {}", kospi);
            } catch (Exception e) {
                log.error("KOSPI 저장 중 오류 발생: {}", e.getMessage());
            }
        }
        return kospis;
    }

    private double parsePercent(String closeStr, String openStr) {
        try {
            double close = Double.parseDouble(closeStr);
            double open = Double.parseDouble(openStr);
            return (close - open) / open * 100.0;
        } catch (Exception e) {
            return 0.0;
        }
    }

    public List<Kospi> getChart(){
        return kospiRepository.findAllByOrderByDateAsc();
    }

}
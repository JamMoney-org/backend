package com.example.jammoney.stockApp.stock.controller;

import com.example.jammoney.stockApp.stock.dto.OrderResponseDto;
import com.example.jammoney.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/long-polling")
public class LongPollingController {

    private static final Map<Long, DeferredResult<ResponseEntity<List<List<OrderResponseDto>>>>> userWaitMap = new ConcurrentHashMap<>();

    /**
     * [사용자의 주문 체결 여부를 감지하여, 체결이 발생하면 실시간으로 응답을 전송하는 롱 폴링 API]
     * - 프론트가 /api/long-polling 엔드포인트에 GET 요청을 보내면,
     *   서버는 주문 체결 이벤트가 발생할 때까지 최대 60초 동안 대기.
     * - 체결 이벤트 발생 시, 해당 사용자의 DeferredResult가 완료되며
     *   최신 주문 내역(BUY/SELL)을 응답으로 전송.
     * - 체결 이벤트가 없으면 60초 후 타임아웃 처리.
     *
     * @return 체결된 주문 정보가 포함된 응답(ResponseEntity<List<[buyOrders], [sellOrders]>>)
     */
    @GetMapping
    public DeferredResult<ResponseEntity<List<List<OrderResponseDto>>>> listen(@AuthenticationPrincipal User user) {
        DeferredResult<ResponseEntity<List<List<OrderResponseDto>>>> output = new DeferredResult<>(60000L);
        userWaitMap.put(user.getId(), output);

        output.onCompletion(() -> userWaitMap.remove(user.getId()));
        output.onTimeout(() -> userWaitMap.remove(user.getId()));

        return output;
    }

    public static void sendUpdate(Long userId, List<OrderResponseDto> buyOrders, List<OrderResponseDto> sellOrders) {
        DeferredResult<ResponseEntity<List<List<OrderResponseDto>>>> result = userWaitMap.remove(userId);
        if (result != null) {
            List<List<OrderResponseDto>> data = List.of(buyOrders, sellOrders);
            result.setResult(ResponseEntity.ok(data));
        }
    }
}

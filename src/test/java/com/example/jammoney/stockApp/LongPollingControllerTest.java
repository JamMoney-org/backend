package com.example.jammoney.stockApp;

import com.example.jammoney.stockApp.stock.controller.LongPollingController;
import com.example.jammoney.stockApp.stock.dto.OrderResponseDto;
import com.example.jammoney.user.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.List;
import java.util.concurrent.ForkJoinPool;

import static org.junit.jupiter.api.Assertions.*;

/*class LongPollingControllerTest {
    /*
    프론트엔드가 롱 폴링 요청을 보냈을 때,
    서버는 주문 체결 이벤트가 발생하면 실시간으로 응답을 내려줄 수 있고,
    체결 이벤트가 없다면 타임아웃으로 요청을 종료시킬 수 있는지 확인하는 테스트

    @Test
    void sendUpdate_shouldCompleteDeferredResult() throws Exception {
        // given
        Long userId = 1L;
        User mockUser = new User();
        mockUser.setId(userId);

        // listen() 호출 시 DeferredResult 등록
        DeferredResult<ResponseEntity<List<List<OrderResponseDto>>>> result =
                new DeferredResult<>(60000L);
        LongPollingControllerTestWrapper.addUserDeferredResult(userId, result);

        // 비동기 응답을 기다리기 위한 쓰레드 설정
        ForkJoinPool.commonPool().submit(() -> {
            try {
                Thread.sleep(100); // 응답 전에 잠깐 대기
                List<OrderResponseDto> buyOrders = List.of(new OrderResponseDto());
                List<OrderResponseDto> sellOrders = List.of(new OrderResponseDto());
                LongPollingController.sendUpdate(userId, buyOrders, sellOrders);
            } catch (InterruptedException ignored) {}
        });

        // when - 최대 2초까지 대기
        int maxWait = 2000;
        int waited = 0;
        while (!result.hasResult() && waited < maxWait) {
            Thread.sleep(100);
            waited += 100;
        }

        // then
        assertTrue(result.hasResult(), "DeferredResult가 결과를 가져야 함");
        @SuppressWarnings("unchecked")
        ResponseEntity<List<List<OrderResponseDto>>> response =
                (ResponseEntity<List<List<OrderResponseDto>>>) result.getResult();
        assertNotNull(response);
        assertEquals(2, response.getBody().size());
    }

    // 내부 상태를 컨트롤하기 위한 Wrapper 클래스 정의
    static class LongPollingControllerTestWrapper extends LongPollingController {
        public static void addUserDeferredResult(Long userId, DeferredResult<ResponseEntity<List<List<OrderResponseDto>>>> result) {
            // 테스트용으로 직접 접근
            userWaitMap.put(userId, result);
        }
    }
    @Test
    void listen_shouldTimeoutAfter60Seconds() throws InterruptedException {
        Long userId = 2L;
        DeferredResult<ResponseEntity<List<List<OrderResponseDto>>>> result =
                new DeferredResult<>(500L); // 테스트용으로 타임아웃 짧게 설정
        LongPollingControllerTestWrapper.addUserDeferredResult(userId, result);

        Thread.sleep(1000); // 타임아웃 대기

        assertFalse(result.hasResult(), "타임아웃 후에도 결과가 없어야 함");
    }
}*/

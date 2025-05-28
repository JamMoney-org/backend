package com.example.jammoney.cash.controller;
import com.example.jammoney.auth.entity.CustomUserDetails;
import com.example.jammoney.cash.service.CashService;
import com.example.jammoney.exception.LevelNotMatchedException;
import com.example.jammoney.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/cash")
public class CashController {
    private final CashService cashService;
    @PostMapping("/init")
    public void initiateCash(@AuthenticationPrincipal CustomUserDetails customUserDetails) {
        User user = customUserDetails.getUser();

        if (user.getPet().getLevel() == 7) {
            cashService.addCash(user.getId(), 3000000L);
        } else {
            throw new LevelNotMatchedException();
        }
    }
}

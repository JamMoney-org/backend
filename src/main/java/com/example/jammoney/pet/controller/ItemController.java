package com.example.jammoney.pet.controller;

import com.example.jammoney.auth.entity.CustomUserDetails;
import com.example.jammoney.exception.ApiResponse;
import com.example.jammoney.pet.dto.*;
import com.example.jammoney.pet.service.ItemService;
import com.example.jammoney.user.entity.User;
import com.example.jammoney.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/item")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;
    private final UserRepository userRepository;

    // 현재 로그인한 유저를 DB에서 다시 가져오는 유틸 메서드
//    private User getCurrentUser() {
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        CustomUserDetails principal = (CustomUserDetails) authentication.getPrincipal();
//
//        return userRepository.findById(principal.getId())
//                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));
//    }

    // 상점 아이템 전체 조회 (모두에게 허용)
    @GetMapping("/shop")
    public ResponseEntity<ApiResponse<List<ItemShopResponseDTO>>> getShopItems() {
        List<ItemShopResponseDTO> shopItems = itemService.getAllShopItems();
        return ResponseEntity.ok(ApiResponse.success("상점 아이템 조회 성공", shopItems));
    }

    // 아이템 구매
    @PostMapping("/purchase")
    public ResponseEntity<ApiResponse<Void>> purchaseItem(@RequestBody ItemPurchaseRequestDTO request,@AuthenticationPrincipal CustomUserDetails userDetails ) {
        //User user = getCurrentUser();
        User user = userDetails.getUser();
        itemService.purchaseItem(user, request.getItemId());
        return ResponseEntity.ok(ApiResponse.success("아이템을 성공적으로 구매했습니다.", null));
    }

    // 인벤토리 조회
    @GetMapping("/inventory")
    public ResponseEntity<ApiResponse<List<InventoryResponseDTO>>> getInventory(@AuthenticationPrincipal CustomUserDetails userDetails) {
        //User user = getCurrentUser();
        User user = userDetails.getUser();
        List<InventoryResponseDTO> inventory = itemService.getUserInventory(user);
        return ResponseEntity.ok(ApiResponse.success("인벤토리 조회 성공", inventory));
    }

    // 아이템 장착/해제
    @PostMapping("/equip")
    public ResponseEntity<ApiResponse<Void>> equipItem(@RequestBody ItemEquipRequestDTO request,@AuthenticationPrincipal CustomUserDetails userDetails) {
        //User user = getCurrentUser();
        User user = userDetails.getUser();
        itemService.equipItem(user, request.getItemId(), request.isEquip());
        String message = request.isEquip() ? "아이템 장착 완료" : "아이템 해제 완료";
        return ResponseEntity.ok(ApiResponse.success(message, null));
    }

    // 아이템 판매
    @PostMapping("/sell")
    public ResponseEntity<ApiResponse<Void>> sellItem(@RequestBody ItemSellRequestDTO request,@AuthenticationPrincipal CustomUserDetails userDetails) {
        //User user = getCurrentUser();
        User user = userDetails.getUser();
        itemService.sellItem(user, request.getItemId());
        return ResponseEntity.ok(ApiResponse.success("아이템 판매 완료", null));
    }
}
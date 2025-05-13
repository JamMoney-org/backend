package com.example.jammoney.pet.controller;

import com.example.jammoney.auth.entity.CustomUserDetails;
import com.example.jammoney.exception.ApiResponse;
import com.example.jammoney.pet.dto.*;
import com.example.jammoney.pet.service.ItemService;
import com.example.jammoney.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/item")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    // 상점 아이템 전체 조회 (모두에게 허용)
    @GetMapping("/shop")
    public ResponseEntity<ApiResponse<List<ItemShopResponseDTO>>> getShopItems() {
        List<ItemShopResponseDTO> shopItems = itemService.getAllShopItems();
        return ResponseEntity.ok(ApiResponse.success("상점 아이템 조회 성공", shopItems));
    }

    // 아이템 구매
    @PostMapping("/purchase")
    public ResponseEntity<ApiResponse<Void>> purchaseItem(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody ItemPurchaseRequestDTO request) {

        User user = userDetails.getUser();
        itemService.purchaseItem(user, request.getItemId());
        return ResponseEntity.ok(ApiResponse.success("아이템을 성공적으로 구매했습니다.", null));
    }

    // 인벤토리 조회
    @GetMapping("/inventory")
    public ResponseEntity<ApiResponse<List<InventoryResponseDTO>>> getInventory(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        User user = userDetails.getUser();
        List<InventoryResponseDTO> inventory = itemService.getUserInventory(user);
        return ResponseEntity.ok(ApiResponse.success("인벤토리 조회 성공", inventory));
    }

    // 아이템 장착/해제
    @PostMapping("/equip")
    public ResponseEntity<ApiResponse<Void>> equipItem(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody ItemEquipRequestDTO request) {

        User user = userDetails.getUser();
        itemService.equipItem(user, request.getItemId(), request.isEquip());
        String message = request.isEquip() ? "아이템 장착 완료" : "아이템 해제 완료";
        return ResponseEntity.ok(ApiResponse.success(message, null));
    }

    // 아이템 판매
    @PostMapping("/sell")
    public ResponseEntity<ApiResponse<Void>> sellItem(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody ItemSellRequestDTO request) {

        User user = userDetails.getUser();
        itemService.sellItem(user, request.getItemId());
        return ResponseEntity.ok(ApiResponse.success("아이템 판매 완료", null));
    }
}
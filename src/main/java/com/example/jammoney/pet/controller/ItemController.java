package com.example.jammoney.pet.controller;

import com.example.jammoney.exception.ApiResponse;
import com.example.jammoney.User.User;
import com.example.jammoney.User.UserRepository;
import com.example.jammoney.pet.dto.*;
import com.example.jammoney.pet.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/item")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;
    private final UserRepository userRepository;

    // 상점 아이템 조회
    @GetMapping("/shop")
    public ResponseEntity<ApiResponse<List<ItemShopResponseDTO>>> getShopItems() {
        List<ItemShopResponseDTO> shopItems = itemService.getAllShopItems();
        return ResponseEntity.ok(ApiResponse.success("상점 아이템 조회 성공", shopItems));
    }

    // 아이템 구매
    @PostMapping("/purchase")
    public ResponseEntity<ApiResponse<Void>> purchaseItem(@RequestBody ItemPurchaseRequestDTO request,
                                                          @RequestParam Long userId) {
        User user = findUserById(userId);
        itemService.purchaseItem(user, request.getItemId());
        return ResponseEntity.ok(ApiResponse.success("아이템을 성공적으로 구매했습니다.", null));
    }

    // 인벤토리 조회
    @GetMapping("/inventory")
    public ResponseEntity<ApiResponse<List<InventoryResponseDTO>>> getInventory(@RequestParam Long userId) {
        User user = findUserById(userId);
        List<InventoryResponseDTO> inventory = itemService.getUserInventory(user);
        return ResponseEntity.ok(ApiResponse.success("인벤토리 조회 성공", inventory));
    }

    // 아이템 장착/해제
    @PostMapping("/equip")
    public ResponseEntity<ApiResponse<Void>> equipItem(@RequestBody ItemEquipRequestDTO request,
                                                       @RequestParam Long userId) {
        User user = findUserById(userId);
        itemService.equipItem(user, request.getItemId(), request.isEquip());
        String message = request.isEquip() ? "아이템 장착 완료" : "아이템 해제 완료";
        return ResponseEntity.ok(ApiResponse.success(message, null));
    }

    // 아이템 판매
    @PostMapping("/sell")
    public ResponseEntity<ApiResponse<Void>> sellItem(@RequestBody ItemSellRequestDTO request,
                                                      @RequestParam Long userId) {
        User user = findUserById(userId);
        itemService.sellItem(user, request.getItemId());
        return ResponseEntity.ok(ApiResponse.success("아이템 판매 완료", null));
    }

    // 유저 조회
    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 유저가 존재하지 않습니다."));
    }
}
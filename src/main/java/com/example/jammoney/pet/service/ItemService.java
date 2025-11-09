package com.example.jammoney.pet.service;

import com.example.jammoney.S3.S3Uploader;
import com.example.jammoney.cash.service.CashService;
import com.example.jammoney.pet.dto.InventoryResponseDTO;
import com.example.jammoney.pet.dto.ItemShopResponseDTO;
import com.example.jammoney.pet.entity.InventoryItem;
import com.example.jammoney.pet.entity.Item;
import com.example.jammoney.pet.entity.ItemType;
import com.example.jammoney.pet.entity.Pet;
import com.example.jammoney.pet.repository.InventoryItemRepository;
import com.example.jammoney.pet.repository.ItemRepository;
import com.example.jammoney.pet.repository.PetRepository;
import com.example.jammoney.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final PetRepository petRepository;
    private final S3Uploader s3Uploader;
    private final CashService cashService;

    // 상점 아이템 목록 조회
    public List<ItemShopResponseDTO> getAllShopItems() {
        return itemRepository.findAll().stream()
                .map(item -> ItemShopResponseDTO.builder()
                        .itemId(item.getId())
                        .name(item.getName())
                        .type(item.getType().name())
                        .price(item.getPrice())
                        .previewUrl(item.getPreviewUrl())
                        .build())
                .collect(Collectors.toList());
    }

    // 아이템 구매
    public void purchaseItem(User user, Long itemId) {
        Pet pet = user.getPet();
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("해당 아이템이 존재하지 않습니다."));

        boolean alreadyOwned = inventoryItemRepository.findByPetAndItem(pet, item).isPresent();
        if (alreadyOwned) {
            throw new IllegalStateException("이미 보유한 아이템입니다.");
        }

        // 충분한 돈 있는지 확인
        if (user.getCash().getMoney() < item.getPrice()) {
            throw new IllegalStateException("가상 머니가 부족합니다.");
        }

        // 코인 차감
        cashService.subtractCash(user.getId(), item.getPrice());

        // 인벤토리에 추가
        InventoryItem inventory = InventoryItem.builder()
                .pet(pet)
                .item(item)
                .equipped(false)
                .build();

        inventoryItemRepository.save(inventory);
    }


    // 인벤토리 조회
    public List<InventoryResponseDTO> getUserInventory(User user) {
        Pet pet = user.getPet();
        List<InventoryItem> items = inventoryItemRepository.findByPet(pet);

        return items.stream().map(inv -> {
            Item item = inv.getItem();
            return InventoryResponseDTO.builder()
                    .itemId(item.getId())
                    .name(item.getName())
                    .type(item.getType().name())
                    .equipped(inv.isEquipped())
                    .imageUrl(item.getImageUrl())
                    .position(item.getPosition())
                    .build();
        }).collect(Collectors.toList());
    }

    // 아이템 장착/해제
    public void equipItem(User user, Long itemId, boolean equip) {
        Pet pet = user.getPet();
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("아이템 없음"));

        InventoryItem target = inventoryItemRepository.findByPetAndItem(pet, item)
                .orElseThrow(() -> new IllegalStateException("보유하지 않은 아이템"));

        if (equip) {
            // 같은 타입 아이템들 전부 해제
            List<InventoryItem> sameTypeItems = inventoryItemRepository.findByPet(pet).stream()
                    .filter(i -> i.getItem().getType() == item.getType())
                    .collect(Collectors.toList());

            sameTypeItems.forEach(i -> i.setEquipped(false));
            target.setEquipped(true);
        } else {
            target.setEquipped(false);
        }

        inventoryItemRepository.save(target);
    }

    // 아이템 판매
    public void sellItem(User user, Long itemId) {
        Pet pet = user.getPet();
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("해당 아이템이 존재하지 않습니다."));

        InventoryItem inventory = inventoryItemRepository.findByPetAndItem(pet, item)
                .orElseThrow(() -> new IllegalStateException("보유하지 않은 아이템입니다."));

        if (inventory.isEquipped()) {
            throw new IllegalStateException("장착 중인 아이템은 판매할 수 없습니다.");
        }

        // 환불 금액 계산 (80% 환급)
        long refund = Math.round(item.getPrice() * 0.8);
        cashService.addCash(user.getId(), refund);

        // 인벤토리에서 제거
        inventoryItemRepository.delete(inventory);
    }

    //아이템 등록
    public void registerItem(String name, int price, ItemType type, String position, String imageUrl, String previewUrl) {
        Item item = Item.builder()
                .name(name)
                .price(price)
                .type(type)
                .position(position)
                .imageUrl(imageUrl)
                .previewUrl(previewUrl)
                .build();

        itemRepository.save(item);
    }
}
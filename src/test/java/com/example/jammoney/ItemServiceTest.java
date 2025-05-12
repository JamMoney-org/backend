package com.example.jammoney;

import com.example.jammoney.StockApp.stock.entity.Cash;
import com.example.jammoney.pet.dto.InventoryResponseDTO;
import com.example.jammoney.pet.dto.ItemShopResponseDTO;
import com.example.jammoney.pet.entity.InventoryItem;
import com.example.jammoney.pet.entity.Item;
import com.example.jammoney.pet.entity.ItemType;
import com.example.jammoney.pet.entity.Pet;
import com.example.jammoney.pet.repository.InventoryItemRepository;
import com.example.jammoney.pet.repository.ItemRepository;
import com.example.jammoney.pet.repository.PetRepository;
import com.example.jammoney.pet.service.ItemService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

    @InjectMocks
    private ItemService itemService;

    @Mock
    private ItemRepository itemRepository;
    @Mock private InventoryItemRepository inventoryItemRepository;
    @Mock private PetRepository petRepository;

    // 상점 목록 조회
    @Test
    void 상점_아이템_리스트_정상조회() {
        List<Item> itemList = List.of(
                Item.builder().id(1L).name("모자").price(100).previewUrl("a.png").type(ItemType.OBJECT).build(),
                Item.builder().id(2L).name("배경").price(200).previewUrl("b.png").type(ItemType.BACKGROUND).build()
        );
        Mockito.when(itemRepository.findAll()).thenReturn(itemList);

        List<ItemShopResponseDTO> result = itemService.getAllShopItems();

        assertEquals(2, result.size());
        assertEquals("모자", result.get(0).getName());
    }

    // 아이템 구매 성공
    @Test
    void 아이템_구매_성공() {
        User user = makeUserWithCash(500);
        Pet pet = user.getPet();

        Item item = Item.builder().id(1L).price(200).name("모자").build();
        Mockito.when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        Mockito.when(inventoryItemRepository.findByPetAndItem(pet, item)).thenReturn(Optional.empty());

        itemService.purchaseItem(user, 1L);

        assertEquals(300, user.getCash().getMoney()); // 500 - 200
        Mockito.verify(inventoryItemRepository).save(any());
    }

    // 아이템 이미 있음
    @Test
    void 이미_보유한_아이템_구매시_예외() {
        User user = makeUserWithCash(500);
        Pet pet = user.getPet();
        Item item = Item.builder().id(1L).price(100).build();

        Mockito.when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        Mockito.when(inventoryItemRepository.findByPetAndItem(pet, item)).thenReturn(Optional.of(new InventoryItem()));

        assertThrows(IllegalStateException.class, () -> itemService.purchaseItem(user, 1L));
    }

    // 인벤토리 조회
    @Test
    void 인벤토리_정상조회() {
        User user = makeUserWithCash(0);
        Pet pet = user.getPet();

        Item item = Item.builder().id(1L).name("배경").type(ItemType.BACKGROUND).imageUrl("img.png").position("bottom").build();
        InventoryItem inventory = InventoryItem.builder().item(item).pet(pet).equipped(true).build();

        Mockito.when(inventoryItemRepository.findByPet(pet)).thenReturn(List.of(inventory));

        List<InventoryResponseDTO> list = itemService.getUserInventory(user);

        assertEquals(1, list.size());
        assertEquals("배경", list.get(0).getName());
        assertTrue(list.get(0).isEquipped());
    }

    // 아이템 장착
    @Test
    void 아이템_장착_성공() {
        User user = makeUserWithCash(0);
        Pet pet = user.getPet();

        Item item = Item.builder().id(1L).type(ItemType.OBJECT).build();
        InventoryItem inventory = InventoryItem.builder().item(item).pet(pet).equipped(false).build();

        Mockito.when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        Mockito.when(inventoryItemRepository.findByPetAndItem(pet, item)).thenReturn(Optional.of(inventory));
        Mockito.when(inventoryItemRepository.findByPet(pet)).thenReturn(List.of(inventory));

        itemService.equipItem(user, 1L, true);

        assertTrue(inventory.isEquipped());
    }

    // 아이템 판매 성공
    @Test
    void 아이템_판매_정상작동() {
        User user = makeUserWithCash(100);
        Pet pet = user.getPet();
        Item item = Item.builder().id(1L).price(200).build();
        InventoryItem inventory = InventoryItem.builder().item(item).pet(pet).equipped(false).build();

        Mockito.when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        Mockito.when(inventoryItemRepository.findByPetAndItem(pet, item)).thenReturn(Optional.of(inventory));

        itemService.sellItem(user, 1L);

        assertEquals(100 + 160, user.getCash().getMoney()); // 80% 환급
        Mockito.verify(inventoryItemRepository).delete(inventory);
    }

    // 장착중이면 판매 불가
    @Test
    void 장착중인_아이템_판매시_예외() {
        User user = makeUserWithCash(100);
        Pet pet = user.getPet();
        Item item = Item.builder().id(1L).price(100).build();
        InventoryItem inventory = InventoryItem.builder().item(item).pet(pet).equipped(true).build();

        Mockito.when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        Mockito.when(inventoryItemRepository.findByPetAndItem(pet, item)).thenReturn(Optional.of(inventory));

        assertThrows(IllegalStateException.class, () -> itemService.sellItem(user, 1L));
    }

    // ---------------- 유틸: 유저 + 캐시 + 펫 생성 ----------------
    private User makeUserWithCash(long money) {
        Cash cash = new Cash();
        cash.setMoney(money);

        Pet pet = Pet.builder().build();
        User user = User.builder().cash(cash).pet(pet).build();

        pet.setUser(user);
        cash.setUser(user);

        return user;
    }
}

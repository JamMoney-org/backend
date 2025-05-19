package com.example.jammoney.stockApp.stock.service;

import com.example.jammoney.stockApp.stock.dto.InterestingStockResponseDto;
import com.example.jammoney.stockApp.stock.entity.InterestingStock;
import com.example.jammoney.stockApp.stock.mapper.StockMapper;
import com.example.jammoney.stockApp.stock.repository.InterestingStockRepository;
import com.example.jammoney.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InterestingStockService {
    private final InterestingStockRepository interestingStockRepository;
    private final CompanyService companyService;
    private final StockMapper stockMapper;

    public void saveInterestingStock(User user, Long companyId) {
        InterestingStock interestingStock = new InterestingStock();
        interestingStock.setUser(user);
        interestingStock.setCompany(companyService.findCompanyById(companyId));

        interestingStockRepository.save(interestingStock);
    }

    public void deleteStar(User user, Long companyId) {
        InterestingStock star = interestingStockRepository.findByUser_IdAndCompany_CompanyId(user.getId(), companyId);

        interestingStockRepository.delete(star);
    }

    public List<InterestingStockResponseDto> getStarResponseDtoList(User user) {
        List<InterestingStock> interestingStocks = interestingStockRepository.findAllByUser_Id(user.getId());
        return stockMapper.interestingStockToDto(interestingStocks);
    }
}

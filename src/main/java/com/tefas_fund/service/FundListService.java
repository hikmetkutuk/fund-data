package com.tefas_fund.service;

import com.tefas_fund.model.FundList;
import com.tefas_fund.repository.FundListRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FundListService {
    private final FundListRepository fundListRepository;

    public FundListService(FundListRepository fundListRepository) {
        this.fundListRepository = fundListRepository;
    }

    public List<String> getAllFunds() {
        return fundListRepository.findAll().stream().map(FundList::getSymbol).toList();
    }
}

package com.tefas_fund.repository;

import com.tefas_fund.model.FundPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface FundRepository extends JpaRepository<FundPrice, Long> {
    Optional<FundPrice> findBySymbolAndDate(String symbol, LocalDate date);
}

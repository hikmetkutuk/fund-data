package com.tefas_fund.repository;

import com.tefas_fund.model.FundPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface FundPriceRepository extends JpaRepository<FundPrice, Long> {
    Optional<FundPrice> findBySymbolAndDate(String symbol, LocalDate date);

    @Query("SELECT f FROM FundPrice f WHERE f.symbol = :symbol AND f.date = :date")
    Optional<FundPrice> findByFundSymbolAndDate(@Param("symbol") String symbol, @Param("date") LocalDate date);

    @Query("SELECT f FROM FundPrice f WHERE f.symbol = :symbol AND f.date <= :date ORDER BY f.date DESC LIMIT 1")
    Optional<FundPrice> findByFundSymbolAndClosestDate(@Param("symbol") String symbol, @Param("date") LocalDate date);
}

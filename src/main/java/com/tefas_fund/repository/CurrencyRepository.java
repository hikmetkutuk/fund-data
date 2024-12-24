package com.tefas_fund.repository;

import com.tefas_fund.model.CurrencyPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface CurrencyRepository extends JpaRepository<CurrencyPrice, Long> {
    boolean existsByCurrencyAndDate(String currency, LocalDate date);

    Optional<CurrencyPrice> findByCurrencyAndDate(String currency, LocalDate date);}

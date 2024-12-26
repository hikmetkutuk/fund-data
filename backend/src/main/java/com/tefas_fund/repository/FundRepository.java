package com.tefas_fund.repository;

import com.tefas_fund.model.Fund;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FundRepository extends JpaRepository<Fund, Long> {
    List<Fund> findAllByIdGreaterThan(Long id);
    Fund findBySymbol(String symbol);
}

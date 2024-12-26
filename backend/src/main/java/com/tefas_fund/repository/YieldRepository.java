package com.tefas_fund.repository;

import com.tefas_fund.model.Yield;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface YieldRepository extends JpaRepository<Yield, Long> {
}

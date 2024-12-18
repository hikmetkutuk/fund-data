package com.tefas_fund.repository;

import com.tefas_fund.model.FundList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FundListRepository extends JpaRepository<FundList, Long> {
    List<FundList> findAllByIdGreaterThan(Long id);
}

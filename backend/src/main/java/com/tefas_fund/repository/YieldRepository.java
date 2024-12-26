package com.tefas_fund.repository;

import com.tefas_fund.model.Yield;
import jakarta.persistence.QueryHint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;

@Repository
public interface YieldRepository extends JpaRepository<Yield, Long>, JpaSpecificationExecutor<Yield> {
    @QueryHints(@QueryHint(name = "org.hibernate.fetchSize", value = "50"))  // Optimize fetch size
    Page<Yield> findAll(Specification<Yield> spec, Pageable pageable);
}

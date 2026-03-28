package com.tefas_fund.util;

import com.tefas_fund.model.Yield;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

public class YieldSpecification implements Specification<Yield> {
    private final String searchTerm;

    public YieldSpecification(String searchTerm) {
        this.searchTerm = searchTerm;
    }

    @Override
    public Predicate toPredicate(@NonNull Root<Yield> root, @Nullable CriteriaQuery<?> query,
                                 @NonNull CriteriaBuilder criteriaBuilder) {
        if (searchTerm == null || searchTerm.isEmpty()) {
            return criteriaBuilder.conjunction();
        }

        String pattern = "%" + searchTerm.toLowerCase() + "%";
        return criteriaBuilder.or(
                criteriaBuilder.like(root.get("symbol"), pattern)
        );
    }
}

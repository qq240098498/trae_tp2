package com.express.repository;

import com.express.entity.PriceRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PriceRuleRepository extends JpaRepository<PriceRule, Long> {

    List<PriceRule> findByTemplateIdAndEnabledTrueOrderByMinWeightAsc(Long templateId);

    List<PriceRule> findByTemplateIdOrderByMinWeightAsc(Long templateId);

    @Query("SELECT pr FROM PriceRule pr WHERE pr.template.id = :templateId AND pr.enabled = true " +
           "AND :weight >= pr.minWeight AND (:weight < pr.maxWeight OR pr.maxWeight = -1) " +
           "ORDER BY pr.minWeight ASC")
    Optional<PriceRule> findMatchingRule(@Param("templateId") Long templateId, @Param("weight") Double weight);
}

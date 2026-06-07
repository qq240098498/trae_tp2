package com.express.repository;

import com.express.entity.PriceTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PriceTemplateRepository extends JpaRepository<PriceTemplate, Long> {

    List<PriceTemplate> findByCompanyIdAndEnabledTrue(Long companyId);

    List<PriceTemplate> findByEnabledTrue();

    Optional<PriceTemplate> findByCompanyIdAndIsDefaultTrueAndEnabledTrue(Long companyId);

    @Modifying
    @Query("UPDATE PriceTemplate pt SET pt.isDefault = false WHERE pt.company.id = :companyId AND pt.isDefault = true")
    void clearDefaultForCompany(@Param("companyId") Long companyId);

    boolean existsByNameAndCompanyId(String name, Long companyId);
}

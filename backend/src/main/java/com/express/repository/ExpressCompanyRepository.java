package com.express.repository;

import com.express.entity.ExpressCompany;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExpressCompanyRepository extends JpaRepository<ExpressCompany, Long> {

    List<ExpressCompany> findByEnabledTrue();

    Optional<ExpressCompany> findByName(String name);

    boolean existsByName(String name);
}

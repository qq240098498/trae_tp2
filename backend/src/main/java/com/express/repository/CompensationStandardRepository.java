package com.express.repository;

import com.express.entity.CompensationStandard;
import com.express.entity.ExceptionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CompensationStandardRepository extends JpaRepository<CompensationStandard, Long> {

    Optional<CompensationStandard> findByExceptionType(ExceptionType exceptionType);

    Optional<CompensationStandard> findByExceptionTypeAndEnabledTrue(ExceptionType exceptionType);
}

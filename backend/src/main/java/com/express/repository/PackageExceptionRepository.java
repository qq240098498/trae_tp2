package com.express.repository;

import com.express.entity.ExceptionStatus;
import com.express.entity.ExceptionType;
import com.express.entity.PackageException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PackageExceptionRepository extends JpaRepository<PackageException, Long> {

    List<PackageException> findByStatus(ExceptionStatus status);

    List<PackageException> findByExceptionType(ExceptionType exceptionType);

    List<PackageException> findByPackageId(Long packageId);

    List<PackageException> findByTrackingNumber(String trackingNumber);

    List<PackageException> findByStatusIn(List<ExceptionStatus> statuses);

    long countByStatus(ExceptionStatus status);

    long countByExceptionType(ExceptionType exceptionType);
}

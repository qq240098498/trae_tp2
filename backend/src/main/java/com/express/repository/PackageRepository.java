package com.express.repository;

import com.express.entity.Package;
import com.express.entity.PackageStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PackageRepository extends JpaRepository<Package, Long> {

    Optional<Package> findByPickupCode(String pickupCode);

    List<Package> findByReceiverPhone(String receiverPhone);

    Optional<Package> findByTrackingNumber(String trackingNumber);

    List<Package> findByStatus(PackageStatus status);

    boolean existsByPickupCode(String pickupCode);

    boolean existsByTrackingNumber(String trackingNumber);

    @Query("SELECT p FROM Package p WHERE p.status = :status AND p.createTime < :time")
    List<Package> findPendingPackagesOlderThan(@Param("status") PackageStatus status, @Param("time") LocalDateTime time);

    @Query("SELECT DATE(p.createTime) as date, COUNT(p) as count FROM Package p GROUP BY DATE(p.createTime) ORDER BY date DESC")
    List<Object[]> countPackagesByDate();

    @Query("SELECT COUNT(p) FROM Package p WHERE DATE(p.createTime) = CURRENT_DATE")
    long countTodayPackages();

    long countByStatus(PackageStatus status);
}

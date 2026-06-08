package com.express.repository;

import com.express.entity.Package;
import com.express.entity.PackageStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
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

    @Query(value = "SELECT CAST(p.create_time AS DATE) as date, COUNT(p.id) as count FROM packages p GROUP BY CAST(p.create_time AS DATE) ORDER BY date DESC", nativeQuery = true)
    List<Object[]> countPackagesByDate();

    @Query(value = "SELECT COUNT(p.id) FROM packages p WHERE CAST(p.create_time AS DATE) = CURRENT_DATE", nativeQuery = true)
    long countTodayPackages();

    long countByStatus(PackageStatus status);

    @Query(value = "SELECT COUNT(p.id) FROM packages p WHERE CAST(p.create_time AS DATE) = :date AND p.courier = :courier", nativeQuery = true)
    long countByDateAndCourier(@Param("date") LocalDate date, @Param("courier") String courier);

    @Query(value = "SELECT COUNT(p.id) FROM packages p WHERE CAST(p.create_time AS DATE) = :date AND p.status = :status AND p.courier = :courier", nativeQuery = true)
    long countByDateAndCourierAndStatus(@Param("date") LocalDate date, @Param("courier") String courier, @Param("status") String status);

    @Query(value = "SELECT DISTINCT p.courier FROM packages p ORDER BY p.courier", nativeQuery = true)
    List<String> findDistinctCouriers();
}

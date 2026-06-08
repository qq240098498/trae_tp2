package com.express.repository;

import com.express.entity.CourierReconciliation;
import com.express.entity.ReconciliationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface CourierReconciliationRepository extends JpaRepository<CourierReconciliation, Long> {

    Optional<CourierReconciliation> findByReconciliationDateAndCourier(LocalDate date, String courier);

    List<CourierReconciliation> findByReconciliationDateOrderByCourier(LocalDate date);

    List<CourierReconciliation> findByReconciliationDateBetweenOrderByReconciliationDateDescCourier(LocalDate startDate, LocalDate endDate);

    List<CourierReconciliation> findByStatusOrderByReconciliationDateDescCourier(ReconciliationStatus status);

    List<CourierReconciliation> findAllByOrderByReconciliationDateDescCourier();

    boolean existsByReconciliationDateAndCourier(LocalDate date, String courier);

    @Query("SELECT cr FROM CourierReconciliation cr WHERE " +
           "(:startDate IS NULL OR cr.reconciliationDate >= :startDate) AND " +
           "(:endDate IS NULL OR cr.reconciliationDate <= :endDate) AND " +
           "(:courier IS NULL OR cr.courier = :courier) AND " +
           "(:status IS NULL OR cr.status = :status) " +
           "ORDER BY cr.reconciliationDate DESC, cr.courier")
    List<CourierReconciliation> findByFilters(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("courier") String courier,
            @Param("status") ReconciliationStatus status);
}

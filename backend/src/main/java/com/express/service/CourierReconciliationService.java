package com.express.service;

import com.express.entity.CourierReconciliation;
import com.express.entity.PackageStatus;
import com.express.entity.ReconciliationStatus;
import com.express.repository.CourierReconciliationRepository;
import com.express.repository.PackageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CourierReconciliationService {

    private final CourierReconciliationRepository reconciliationRepository;
    private final PackageRepository packageRepository;

    @Transactional
    public List<CourierReconciliation> generateReconciliation(LocalDate date) {
        List<String> couriers = packageRepository.findDistinctCouriers();
        List<CourierReconciliation> results = new ArrayList<>();

        for (String courier : couriers) {
            CourierReconciliation existing = reconciliationRepository
                    .findByReconciliationDateAndCourier(date, courier)
                    .orElse(null);

            long totalIn = packageRepository.countByDateAndCourier(date, courier);
            long totalPickedUp = packageRepository.countByDateAndCourierAndStatus(date, courier, PackageStatus.PICKED_UP.name());
            long totalOverdue = packageRepository.countByDateAndCourierAndStatus(date, courier, PackageStatus.OVERDUE.name());

            if (existing != null) {
                if (existing.getStatus() == ReconciliationStatus.CONFIRMED) {
                    continue;
                }
                existing.setTotalIn((int) totalIn);
                existing.setTotalPickedUp((int) totalPickedUp);
                existing.setTotalOverdue((int) totalOverdue);
                results.add(reconciliationRepository.save(existing));
            } else {
                CourierReconciliation record = new CourierReconciliation();
                record.setReconciliationDate(date);
                record.setCourier(courier);
                record.setTotalIn((int) totalIn);
                record.setTotalPickedUp((int) totalPickedUp);
                record.setTotalOverdue((int) totalOverdue);
                record.setStatus(ReconciliationStatus.PENDING);
                results.add(reconciliationRepository.save(record));
            }
        }

        return results;
    }

    public List<CourierReconciliation> getReconciliations(LocalDate startDate, LocalDate endDate,
                                                           String courier, ReconciliationStatus status) {
        return reconciliationRepository.findByFilters(startDate, endDate, courier, status);
    }

    @Transactional
    public CourierReconciliation confirmReconciliation(Long id) {
        CourierReconciliation record = reconciliationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("对账记录不存在: " + id));

        if (record.getStatus() == ReconciliationStatus.CONFIRMED) {
            throw new IllegalStateException("该对账记录已确认，不可重复操作");
        }

        record.setStatus(ReconciliationStatus.CONFIRMED);
        record.setConfirmTime(LocalDateTime.now());
        return reconciliationRepository.save(record);
    }

    @Transactional
    public CourierReconciliation updateRemark(Long id, String remark) {
        CourierReconciliation record = reconciliationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("对账记录不存在: " + id));

        record.setRemark(remark);
        return reconciliationRepository.save(record);
    }

    public CourierReconciliation getById(Long id) {
        return reconciliationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("对账记录不存在: " + id));
    }

    public List<String> getCourierList() {
        return packageRepository.findDistinctCouriers();
    }
}

package com.express.service;

import com.express.dto.DailyStatsResponse;
import com.express.dto.PackageCreateRequest;
import com.express.dto.PackagePickupRequest;
import com.express.entity.Package;
import com.express.entity.PackageStatus;
import com.express.repository.PackageRepository;
import javax.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class PackageService {

    private final PackageRepository packageRepository;
    private final Random random = new Random();

    @Transactional
    public Package createPackage(PackageCreateRequest request) {
        if (packageRepository.existsByTrackingNumber(request.getTrackingNumber())) {
            throw new IllegalArgumentException("该快递单号已存在: " + request.getTrackingNumber());
        }

        Package pkg = new Package();
        pkg.setTrackingNumber(request.getTrackingNumber());
        pkg.setCourier(request.getCourier());
        pkg.setReceiverName(request.getReceiverName());
        pkg.setReceiverPhone(request.getReceiverPhone());
        pkg.setShelfLocation(request.getShelfLocation());
        pkg.setPickupCode(generateUniquePickupCode());
        pkg.setStatus(PackageStatus.PENDING);

        return packageRepository.save(pkg);
    }

    @Transactional
    public Package pickupPackage(Long id, PackagePickupRequest request) {
        Package pkg = packageRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("包裹不存在: " + id));

        if (pkg.getStatus() == PackageStatus.PICKED_UP) {
            throw new IllegalStateException("该包裹已被取走");
        }

        boolean isValid = false;
        if (request.getPickupCode() != null && !request.getPickupCode().isEmpty()) {
            isValid = pkg.getPickupCode().equals(request.getPickupCode());
        } else if (request.getPhone() != null && !request.getPhone().isEmpty()) {
            isValid = pkg.getReceiverPhone().equals(request.getPhone());
        }

        if (!isValid) {
            throw new IllegalArgumentException("取件码或手机号不正确");
        }

        pkg.setStatus(PackageStatus.PICKED_UP);
        pkg.setPickupTime(LocalDateTime.now());

        return packageRepository.save(pkg);
    }

    public Package getPackageById(Long id) {
        return packageRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("包裹不存在: " + id));
    }

    public List<Package> searchPackages(String phone, String trackingNumber, String pickupCode, PackageStatus status) {
        updateOverduePackages();

        if (pickupCode != null && !pickupCode.isEmpty()) {
            return packageRepository.findByPickupCode(pickupCode)
                    .map(Collections::singletonList)
                    .orElse(Collections.emptyList());
        }

        if (trackingNumber != null && !trackingNumber.isEmpty()) {
            return packageRepository.findByTrackingNumber(trackingNumber)
                    .map(Collections::singletonList)
                    .orElse(Collections.emptyList());
        }

        if (phone != null && !phone.isEmpty()) {
            return packageRepository.findByReceiverPhone(phone);
        }

        if (status != null) {
            return packageRepository.findByStatus(status);
        }

        return packageRepository.findAll();
    }

    public List<Package> getOverduePackages() {
        updateOverduePackages();
        return packageRepository.findByStatus(PackageStatus.OVERDUE);
    }

    @Transactional
    public void updateOverduePackages() {
        LocalDateTime threshold = LocalDateTime.now().minusHours(48);
        List<Package> overduePackages = packageRepository.findPendingPackagesOlderThan(
                PackageStatus.PENDING, threshold);

        for (Package pkg : overduePackages) {
            pkg.setStatus(PackageStatus.OVERDUE);
            packageRepository.save(pkg);
        }
    }

    public List<DailyStatsResponse> getDailyStats() {
        List<Object[]> results = packageRepository.countPackagesByDate();
        List<DailyStatsResponse> stats = new ArrayList<>();

        for (Object[] result : results) {
            LocalDate date = (LocalDate) result[0];
            Long count = (Long) result[1];
            stats.add(new DailyStatsResponse(date, count));
        }

        return stats;
    }

    private String generateUniquePickupCode() {
        String code;
        do {
            code = String.format("%06d", random.nextInt(1000000));
        } while (packageRepository.existsByPickupCode(code));
        return code;
    }
}

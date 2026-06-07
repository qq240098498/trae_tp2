package com.express.controller;

import com.express.entity.PackageStatus;
import com.express.entity.ShipmentStatus;
import com.express.repository.PackageRepository;
import com.express.repository.ShipmentRepository;
import com.express.service.PackageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final PackageRepository packageRepository;
    private final ShipmentRepository shipmentRepository;
    private final PackageService packageService;

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Long>> getDashboardStats() {
        packageService.updateOverduePackages();

        Map<String, Long> stats = new HashMap<>();
        stats.put("todayIn", packageRepository.countTodayPackages());
        stats.put("pendingPickup", packageRepository.countByStatus(PackageStatus.PENDING));
        stats.put("overdue", packageRepository.countByStatus(PackageStatus.OVERDUE));
        stats.put("pendingSend", shipmentRepository.countByStatus(ShipmentStatus.PENDING));

        return ResponseEntity.ok(stats);
    }
}

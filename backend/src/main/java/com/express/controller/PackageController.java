package com.express.controller;

import com.express.dto.DailyStatsResponse;
import com.express.dto.PackageCreateRequest;
import com.express.dto.PackagePickupRequest;
import com.express.entity.Package;
import com.express.entity.PackageStatus;
import com.express.service.PackageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/packages")
@RequiredArgsConstructor
public class PackageController {

    private final PackageService packageService;

    @PostMapping
    public ResponseEntity<Package> createPackage(@RequestBody PackageCreateRequest request) {
        Package createdPackage = packageService.createPackage(request);
        return new ResponseEntity<>(createdPackage, HttpStatus.CREATED);
    }

    @PutMapping("/{id}/pickup")
    public ResponseEntity<Package> pickupPackage(
            @PathVariable Long id,
            @RequestBody PackagePickupRequest request) {
        Package pkg = packageService.pickupPackage(id, request);
        return ResponseEntity.ok(pkg);
    }

    @GetMapping
    public ResponseEntity<List<Package>> searchPackages(
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String trackingNumber,
            @RequestParam(required = false) String pickupCode,
            @RequestParam(required = false) PackageStatus status) {
        List<Package> packages = packageService.searchPackages(phone, trackingNumber, pickupCode, status);
        return ResponseEntity.ok(packages);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Package> getPackageById(@PathVariable Long id) {
        Package pkg = packageService.getPackageById(id);
        return ResponseEntity.ok(pkg);
    }

    @GetMapping("/overdue")
    public ResponseEntity<List<Package>> getOverduePackages() {
        List<Package> packages = packageService.getOverduePackages();
        return ResponseEntity.ok(packages);
    }

    @GetMapping("/stats/daily")
    public ResponseEntity<List<DailyStatsResponse>> getDailyStats() {
        List<DailyStatsResponse> stats = packageService.getDailyStats();
        return ResponseEntity.ok(stats);
    }
}

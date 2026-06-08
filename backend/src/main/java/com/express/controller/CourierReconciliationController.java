package com.express.controller;

import com.express.entity.CourierReconciliation;
import com.express.entity.ReconciliationStatus;
import com.express.service.CourierReconciliationService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reconciliation")
@RequiredArgsConstructor
public class CourierReconciliationController {

    private final CourierReconciliationService reconciliationService;

    @GetMapping
    public ResponseEntity<List<CourierReconciliation>> getReconciliations(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String courier,
            @RequestParam(required = false) ReconciliationStatus status) {
        List<CourierReconciliation> list = reconciliationService.getReconciliations(startDate, endDate, courier, status);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CourierReconciliation> getById(@PathVariable Long id) {
        CourierReconciliation record = reconciliationService.getById(id);
        return ResponseEntity.ok(record);
    }

    @PostMapping("/generate")
    public ResponseEntity<List<CourierReconciliation>> generateReconciliation(
            @RequestBody Map<String, String> body) {
        String dateStr = body.get("date");
        LocalDate date = dateStr != null ? LocalDate.parse(dateStr) : LocalDate.now();
        List<CourierReconciliation> records = reconciliationService.generateReconciliation(date);
        return ResponseEntity.ok(records);
    }

    @PutMapping("/{id}/confirm")
    public ResponseEntity<CourierReconciliation> confirmReconciliation(@PathVariable Long id) {
        CourierReconciliation record = reconciliationService.confirmReconciliation(id);
        return ResponseEntity.ok(record);
    }

    @PutMapping("/{id}/remark")
    public ResponseEntity<CourierReconciliation> updateRemark(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        String remark = body.get("remark");
        CourierReconciliation record = reconciliationService.updateRemark(id, remark);
        return ResponseEntity.ok(record);
    }

    @GetMapping("/couriers")
    public ResponseEntity<List<String>> getCourierList() {
        List<String> couriers = reconciliationService.getCourierList();
        return ResponseEntity.ok(couriers);
    }
}

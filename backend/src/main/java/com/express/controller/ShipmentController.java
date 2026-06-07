package com.express.controller;

import com.express.dto.ShipmentCreateRequest;
import com.express.entity.Shipment;
import com.express.entity.ShipmentStatus;
import com.express.service.ShipmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/shipments")
@RequiredArgsConstructor
public class ShipmentController {

    private final ShipmentService shipmentService;

    @PostMapping
    public ResponseEntity<Shipment> createShipment(@RequestBody ShipmentCreateRequest request) {
        Shipment shipment = shipmentService.createShipment(request);
        return new ResponseEntity<>(shipment, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<Shipment>> getAllShipments(
            @RequestParam(required = false) ShipmentStatus status) {
        List<Shipment> shipments = shipmentService.getAllShipments(status);
        return ResponseEntity.ok(shipments);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Shipment> getShipmentById(@PathVariable Long id) {
        Shipment shipment = shipmentService.getShipmentById(id);
        return ResponseEntity.ok(shipment);
    }

    @PutMapping("/{id}/ship")
    public ResponseEntity<Shipment> markAsShipped(@PathVariable Long id) {
        Shipment shipment = shipmentService.markAsShipped(id);
        return ResponseEntity.ok(shipment);
    }
}

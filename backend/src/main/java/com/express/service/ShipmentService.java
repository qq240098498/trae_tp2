package com.express.service;

import com.express.dto.ShipmentCreateRequest;
import com.express.entity.Shipment;
import com.express.entity.ShipmentStatus;
import com.express.repository.ShipmentRepository;
import javax.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ShipmentService {

    private final ShipmentRepository shipmentRepository;

    @Transactional
    public Shipment createShipment(ShipmentCreateRequest request) {
        Shipment shipment = new Shipment();
        shipment.setSenderName(request.getSenderName());
        shipment.setSenderPhone(request.getSenderPhone());
        shipment.setReceiverName(request.getReceiverName());
        shipment.setReceiverPhone(request.getReceiverPhone());
        shipment.setAddress(request.getAddress());
        shipment.setWeight(request.getWeight());
        shipment.setStatus(ShipmentStatus.PENDING);

        return shipmentRepository.save(shipment);
    }

    @Transactional
    public Shipment markAsShipped(Long id) {
        Shipment shipment = shipmentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("代寄件不存在: " + id));

        if (shipment.getStatus() == ShipmentStatus.SHIPPED) {
            throw new IllegalStateException("该代寄件已标记为已寄出");
        }

        shipment.setStatus(ShipmentStatus.SHIPPED);
        return shipmentRepository.save(shipment);
    }

    public List<Shipment> getAllShipments(ShipmentStatus status) {
        if (status != null) {
            return shipmentRepository.findByStatus(status);
        }
        return shipmentRepository.findAll();
    }

    public Shipment getShipmentById(Long id) {
        return shipmentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("代寄件不存在: " + id));
    }
}

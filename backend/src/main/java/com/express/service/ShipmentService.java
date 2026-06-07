package com.express.service;

import com.express.dto.PriceCalculateRequest;
import com.express.dto.PriceCalculateResponse;
import com.express.dto.ShipmentCreateRequest;
import com.express.entity.*;
import com.express.repository.ShipmentRepository;
import javax.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ShipmentService {

    private final ShipmentRepository shipmentRepository;
    private final PricingService pricingService;

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

        if (request.getWeight() != null && (request.getCompanyId() != null || request.getTemplateId() != null)) {
            PriceCalculateRequest calculateRequest = new PriceCalculateRequest();
            calculateRequest.setCompanyId(request.getCompanyId());
            calculateRequest.setTemplateId(request.getTemplateId());
            calculateRequest.setWeight(request.getWeight());

            PriceCalculateResponse priceResponse = pricingService.calculatePrice(calculateRequest);
            shipment.setFreight(priceResponse.getTotalPrice());

            if (request.getTemplateId() != null) {
                PriceTemplate template = pricingService.getTemplateById(request.getTemplateId());
                shipment.setTemplate(template);
                shipment.setCompany(template.getCompany());
            } else if (request.getCompanyId() != null) {
                ExpressCompany company = pricingService.getCompanyById(request.getCompanyId());
                shipment.setCompany(company);
                PriceTemplate defaultTemplate = pricingService.getTemplatesByCompany(request.getCompanyId(), true)
                        .stream()
                        .filter(PriceTemplate::getIsDefault)
                        .findFirst()
                        .orElse(null);
                shipment.setTemplate(defaultTemplate);
            }
        }

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
        shipment.setShipTime(LocalDateTime.now());
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

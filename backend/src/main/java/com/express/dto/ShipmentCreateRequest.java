package com.express.dto;

import lombok.Data;

@Data
public class ShipmentCreateRequest {
    private String senderName;
    private String senderPhone;
    private String receiverName;
    private String receiverPhone;
    private String address;
    private Double weight;
    private Long companyId;
    private Long templateId;
    private Long memberId;
}

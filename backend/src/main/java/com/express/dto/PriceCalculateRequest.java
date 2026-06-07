package com.express.dto;

import lombok.Data;

@Data
public class PriceCalculateRequest {
    private Long companyId;
    private Long templateId;
    private Double weight;
}

package com.express.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class PriceRuleRequest {
    private Long templateId;
    private Double minWeight;
    private Double maxWeight;
    private BigDecimal basePrice;
    private BigDecimal additionalPrice;
    private Double additionalWeightStep;
    private Boolean enabled;
}

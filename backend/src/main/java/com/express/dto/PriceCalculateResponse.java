package com.express.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class PriceCalculateResponse {
    private BigDecimal totalPrice;
    private BigDecimal basePrice;
    private BigDecimal additionalPrice;
    private Double weight;
    private String companyName;
    private String templateName;
}

package com.express.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CompensationStandardRequest {

    private String exceptionType;

    private BigDecimal standardAmount;

    private String description;

    private Boolean enabled;
}

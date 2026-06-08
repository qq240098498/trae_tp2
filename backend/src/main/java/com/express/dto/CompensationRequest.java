package com.express.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CompensationRequest {

    private Long exceptionId;

    private BigDecimal compensationAmount;

    private String compensationMethod;

    private String handleRemark;
}

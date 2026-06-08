package com.express.dto;

import lombok.Data;

@Data
public class PaymentRequest {

    private Long exceptionId;

    private String paymentOperator;

    private String paymentRemark;
}

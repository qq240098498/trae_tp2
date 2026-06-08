package com.express.dto;

import lombok.Data;

@Data
public class PackageExceptionCreateRequest {

    private Long packageId;

    private String trackingNumber;

    private String orderId;

    private String exceptionType;

    private String description;

    private String reporter;
}

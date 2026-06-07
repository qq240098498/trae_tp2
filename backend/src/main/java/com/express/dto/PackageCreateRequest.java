package com.express.dto;

import lombok.Data;

@Data
public class PackageCreateRequest {
    private String trackingNumber;
    private String courier;
    private String receiverName;
    private String receiverPhone;
    private String shelfLocation;
}

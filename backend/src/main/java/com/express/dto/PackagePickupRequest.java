package com.express.dto;

import lombok.Data;

@Data
public class PackagePickupRequest {
    private String pickupCode;
    private String phone;
}

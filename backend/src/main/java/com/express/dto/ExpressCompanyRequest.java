package com.express.dto;

import lombok.Data;

@Data
public class ExpressCompanyRequest {
    private String name;
    private String code;
    private String contactPhone;
    private Boolean enabled;
}

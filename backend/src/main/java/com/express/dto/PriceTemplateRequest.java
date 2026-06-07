package com.express.dto;

import lombok.Data;

@Data
public class PriceTemplateRequest {
    private String name;
    private String description;
    private Long companyId;
    private Boolean isDefault;
    private Boolean enabled;
}

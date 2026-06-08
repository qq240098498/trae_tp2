package com.express.dto;

import lombok.Data;

@Data
public class PackageExceptionUpdateRequest {

    private String status;

    private String handler;

    private String handleRemark;
}

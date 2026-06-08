package com.express.dto;

import lombok.Data;

@Data
public class ApprovalRequest {

    private Long exceptionId;

    private Boolean approved;

    private String approver;

    private String approvalRemark;
}

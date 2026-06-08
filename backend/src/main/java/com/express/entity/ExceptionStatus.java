package com.express.entity;

public enum ExceptionStatus {
    PENDING,
    PROCESSING,
    COMPENSATION_PENDING,
    APPROVAL_PENDING,
    APPROVED,
    REJECTED,
    PAYMENT_PENDING,
    PAID,
    CLOSED
}

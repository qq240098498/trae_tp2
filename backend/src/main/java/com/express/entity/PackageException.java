package com.express.entity;

import javax.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "package_exceptions")
public class PackageException {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long packageId;

    @Column(nullable = false)
    private String trackingNumber;

    @Column(nullable = false)
    private String orderId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExceptionType exceptionType;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private String reporter;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExceptionStatus status;

    private String handler;

    private String handleRemark;

    private BigDecimal compensationAmount;

    private String compensationMethod;

    private LocalDateTime compensationTime;

    private String approver;

    private LocalDateTime approvalTime;

    private String approvalRemark;

    private String paymentOperator;

    private LocalDateTime paymentTime;

    private String paymentRemark;

    @Column(nullable = false)
    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        if (status == null) {
            status = ExceptionStatus.PENDING;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }
}

package com.express.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "courier_reconciliations", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"reconciliation_date", "courier"})
})
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class CourierReconciliation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "reconciliation_date", nullable = false)
    private LocalDate reconciliationDate;

    @Column(nullable = false)
    private String courier;

    @Column(name = "total_in", nullable = false)
    private Integer totalIn = 0;

    @Column(name = "total_picked_up", nullable = false)
    private Integer totalPickedUp = 0;

    @Column(name = "total_overdue", nullable = false)
    private Integer totalOverdue = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReconciliationStatus status;

    @Column(name = "create_time", nullable = false)
    private LocalDateTime createTime;

    @Column(name = "confirm_time")
    private LocalDateTime confirmTime;

    private String remark;

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        if (status == null) {
            status = ReconciliationStatus.PENDING;
        }
    }
}

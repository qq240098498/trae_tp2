package com.express.service;

import com.express.dto.ApprovalRequest;
import com.express.dto.CompensationRequest;
import com.express.dto.PackageExceptionCreateRequest;
import com.express.dto.PackageExceptionUpdateRequest;
import com.express.dto.PaymentRequest;
import com.express.entity.ExceptionStatus;
import com.express.entity.ExceptionType;
import com.express.entity.Package;
import com.express.entity.PackageException;
import com.express.repository.PackageExceptionRepository;
import com.express.repository.PackageRepository;
import javax.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PackageExceptionService {

    private final PackageExceptionRepository exceptionRepository;

    private final PackageRepository packageRepository;

    @Transactional
    public PackageException createException(PackageExceptionCreateRequest request) {
        if (request.getOrderId() == null || request.getOrderId().isEmpty()) {
            throw new IllegalArgumentException("订单号不能为空，异常登记必须绑定订单");
        }

        if (request.getPackageId() == null) {
            throw new IllegalArgumentException("包裹ID不能为空，异常登记必须绑定包裹");
        }

        Package pkg = packageRepository.findById(request.getPackageId())
                .orElseThrow(() -> new IllegalArgumentException("包裹不存在: " + request.getPackageId()));

        if (!pkg.getTrackingNumber().equals(request.getTrackingNumber())) {
            throw new IllegalArgumentException("快递单号与包裹信息不匹配");
        }

        PackageException exception = new PackageException();
        exception.setTrackingNumber(request.getTrackingNumber());
        exception.setOrderId(request.getOrderId());
        exception.setPackageId(request.getPackageId());
        exception.setExceptionType(ExceptionType.valueOf(request.getExceptionType()));
        exception.setDescription(request.getDescription());
        exception.setReporter(request.getReporter());
        exception.setStatus(ExceptionStatus.PENDING);

        return exceptionRepository.save(exception);
    }

    public PackageException getExceptionById(Long id) {
        return exceptionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("异常记录不存在: " + id));
    }

    public List<PackageException> searchExceptions(String trackingNumber, ExceptionType exceptionType,
                                                    ExceptionStatus status, String reporter) {
        if (trackingNumber != null && !trackingNumber.isEmpty()) {
            return exceptionRepository.findByTrackingNumber(trackingNumber);
        }

        if (status != null) {
            return exceptionRepository.findByStatus(status);
        }

        if (exceptionType != null) {
            return exceptionRepository.findByExceptionType(exceptionType);
        }

        return exceptionRepository.findAll();
    }

    public List<PackageException> getPendingExceptions() {
        return exceptionRepository.findByStatusIn(
                Arrays.asList(ExceptionStatus.PENDING, ExceptionStatus.PROCESSING));
    }

    @Transactional
    public PackageException updateExceptionStatus(Long id, PackageExceptionUpdateRequest request) {
        PackageException exception = getExceptionById(id);

        if (request.getStatus() != null && !request.getStatus().isEmpty()) {
            ExceptionStatus newStatus = ExceptionStatus.valueOf(request.getStatus());
            if (newStatus == ExceptionStatus.PROCESSING && exception.getStatus() != ExceptionStatus.PENDING && exception.getStatus() != ExceptionStatus.REJECTED) {
                throw new IllegalStateException("只有待处理或审批驳回的异常才能转为处理中");
            }
            if (newStatus == ExceptionStatus.CLOSED && exception.getStatus() != ExceptionStatus.PAID) {
                throw new IllegalStateException("只有已打款的异常才能关闭");
            }
            exception.setStatus(newStatus);
        }

        if (request.getHandler() != null && !request.getHandler().isEmpty()) {
            exception.setHandler(request.getHandler());
        }

        if (request.getHandleRemark() != null && !request.getHandleRemark().isEmpty()) {
            exception.setHandleRemark(request.getHandleRemark());
        }

        return exceptionRepository.save(exception);
    }

    @Transactional
    public PackageException compensate(CompensationRequest request) {
        PackageException exception = getExceptionById(request.getExceptionId());

        if (exception.getStatus() != ExceptionStatus.PROCESSING) {
            throw new IllegalStateException("只有处理中的异常才能进行补偿");
        }

        if (request.getCompensationAmount() == null || request.getCompensationMethod() == null) {
            throw new IllegalArgumentException("补偿金额和补偿方式不能为空");
        }

        exception.setCompensationAmount(request.getCompensationAmount());
        exception.setCompensationMethod(request.getCompensationMethod());
        exception.setCompensationTime(LocalDateTime.now());

        if (request.getHandleRemark() != null && !request.getHandleRemark().isEmpty()) {
            exception.setHandleRemark(request.getHandleRemark());
        }

        exception.setStatus(ExceptionStatus.APPROVAL_PENDING);

        return exceptionRepository.save(exception);
    }

    @Transactional
    public PackageException approve(ApprovalRequest request) {
        PackageException exception = getExceptionById(request.getExceptionId());

        if (exception.getStatus() != ExceptionStatus.APPROVAL_PENDING) {
            throw new IllegalStateException("只有待审批的异常才能进行审批");
        }

        if (request.getApproved() == null) {
            throw new IllegalArgumentException("请指定审批结果");
        }

        exception.setApprover(request.getApprover());
        exception.setApprovalTime(LocalDateTime.now());

        if (request.getApprovalRemark() != null && !request.getApprovalRemark().isEmpty()) {
            exception.setApprovalRemark(request.getApprovalRemark());
        }

        if (request.getApproved()) {
            exception.setStatus(ExceptionStatus.APPROVED);
        } else {
            exception.setStatus(ExceptionStatus.REJECTED);
        }

        return exceptionRepository.save(exception);
    }

    @Transactional
    public PackageException payment(PaymentRequest request) {
        PackageException exception = getExceptionById(request.getExceptionId());

        if (exception.getStatus() != ExceptionStatus.APPROVED) {
            throw new IllegalStateException("只有审批通过的异常才能进行打款");
        }

        exception.setPaymentOperator(request.getPaymentOperator());
        exception.setPaymentTime(LocalDateTime.now());

        if (request.getPaymentRemark() != null && !request.getPaymentRemark().isEmpty()) {
            exception.setPaymentRemark(request.getPaymentRemark());
        }

        exception.setStatus(ExceptionStatus.PAID);

        return exceptionRepository.save(exception);
    }

    public long countByStatus(ExceptionStatus status) {
        return exceptionRepository.countByStatus(status);
    }

    public long countByType(ExceptionType type) {
        return exceptionRepository.countByExceptionType(type);
    }

    public List<PackageException> getCompensationList() {
        return exceptionRepository.findByStatusIn(
                Arrays.asList(ExceptionStatus.APPROVAL_PENDING, ExceptionStatus.APPROVED,
                        ExceptionStatus.REJECTED, ExceptionStatus.PAYMENT_PENDING,
                        ExceptionStatus.PAID, ExceptionStatus.CLOSED));
    }
}

package com.express.service;

import com.express.dto.CompensationRequest;
import com.express.dto.PackageExceptionCreateRequest;
import com.express.dto.PackageExceptionUpdateRequest;
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
        PackageException exception = new PackageException();
        exception.setTrackingNumber(request.getTrackingNumber());
        exception.setOrderNumber(request.getOrderNumber());
        exception.setExceptionType(ExceptionType.valueOf(request.getExceptionType()));
        exception.setDescription(request.getDescription());
        exception.setReporter(request.getReporter());
        exception.setStatus(ExceptionStatus.PENDING);

        if (request.getTrackingNumber() != null && !request.getTrackingNumber().isEmpty()) {
            packageRepository.findByTrackingNumber(request.getTrackingNumber()).ifPresent(pkg -> {
                exception.setPackageId(pkg.getId());
            });
        }

        if (exception.getPackageId() == null && request.getPackageId() != null) {
            exception.setPackageId(request.getPackageId());
        }

        if (exception.getPackageId() == null) {
            throw new IllegalArgumentException("未找到对应入库包裹，请确认快递单号正确");
        }

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
            if (newStatus == ExceptionStatus.PROCESSING && exception.getStatus() != ExceptionStatus.PENDING) {
                throw new IllegalStateException("只有待处理的异常才能转为处理中");
            }
            if (newStatus == ExceptionStatus.RESOLVED && exception.getStatus() != ExceptionStatus.PROCESSING) {
                throw new IllegalStateException("只有处理中的异常才能转为已解决");
            }
            if (newStatus == ExceptionStatus.CLOSED && exception.getStatus() != ExceptionStatus.RESOLVED) {
                throw new IllegalStateException("只有已解决的异常才能关闭");
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

        exception.setStatus(ExceptionStatus.RESOLVED);

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
                Arrays.asList(ExceptionStatus.RESOLVED, ExceptionStatus.CLOSED));
    }
}

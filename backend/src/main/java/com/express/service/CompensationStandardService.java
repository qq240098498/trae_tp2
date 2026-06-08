package com.express.service;

import com.express.dto.CompensationStandardRequest;
import com.express.entity.CompensationStandard;
import com.express.entity.ExceptionType;
import com.express.repository.CompensationStandardRepository;
import javax.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CompensationStandardService {

    private final CompensationStandardRepository standardRepository;

    @Transactional
    public CompensationStandard createStandard(CompensationStandardRequest request) {
        ExceptionType type = ExceptionType.valueOf(request.getExceptionType());
        standardRepository.findByExceptionType(type).ifPresent(s -> {
            throw new IllegalArgumentException("该异常类型已有补偿标准，请编辑已有记录");
        });

        CompensationStandard standard = new CompensationStandard();
        standard.setExceptionType(type);
        standard.setStandardAmount(request.getStandardAmount());
        standard.setDescription(request.getDescription());
        standard.setEnabled(request.getEnabled() != null ? request.getEnabled() : true);

        return standardRepository.save(standard);
    }

    public CompensationStandard getStandardById(Long id) {
        return standardRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("补偿标准不存在: " + id));
    }

    public CompensationStandard getStandardByType(ExceptionType type) {
        return standardRepository.findByExceptionTypeAndEnabledTrue(type)
                .orElse(null);
    }

    public List<CompensationStandard> getAllStandards() {
        return standardRepository.findAll();
    }

    @Transactional
    public CompensationStandard updateStandard(Long id, CompensationStandardRequest request) {
        CompensationStandard standard = getStandardById(id);

        if (request.getExceptionType() != null && !request.getExceptionType().isEmpty()) {
            ExceptionType newType = ExceptionType.valueOf(request.getExceptionType());
            if (!newType.equals(standard.getExceptionType())) {
                standardRepository.findByExceptionType(newType).ifPresent(s -> {
                    throw new IllegalArgumentException("该异常类型已有补偿标准");
                });
                standard.setExceptionType(newType);
            }
        }

        if (request.getStandardAmount() != null) {
            standard.setStandardAmount(request.getStandardAmount());
        }

        if (request.getDescription() != null) {
            standard.setDescription(request.getDescription());
        }

        if (request.getEnabled() != null) {
            standard.setEnabled(request.getEnabled());
        }

        return standardRepository.save(standard);
    }

    @Transactional
    public void deleteStandard(Long id) {
        if (!standardRepository.existsById(id)) {
            throw new EntityNotFoundException("补偿标准不存在: " + id);
        }
        standardRepository.deleteById(id);
    }
}

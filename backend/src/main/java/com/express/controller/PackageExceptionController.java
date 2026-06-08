package com.express.controller;

import com.express.dto.CompensationRequest;
import com.express.dto.PackageExceptionCreateRequest;
import com.express.dto.PackageExceptionUpdateRequest;
import com.express.entity.ExceptionStatus;
import com.express.entity.ExceptionType;
import com.express.entity.PackageException;
import com.express.service.PackageExceptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/exceptions")
@RequiredArgsConstructor
public class PackageExceptionController {

    private final PackageExceptionService exceptionService;

    @PostMapping
    public ResponseEntity<PackageException> createException(@RequestBody PackageExceptionCreateRequest request) {
        PackageException exception = exceptionService.createException(request);
        return new ResponseEntity<>(exception, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<PackageException>> searchExceptions(
            @RequestParam(required = false) String trackingNumber,
            @RequestParam(required = false) ExceptionType exceptionType,
            @RequestParam(required = false) ExceptionStatus status,
            @RequestParam(required = false) String reporter) {
        List<PackageException> exceptions = exceptionService.searchExceptions(
                trackingNumber, exceptionType, status, reporter);
        return ResponseEntity.ok(exceptions);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PackageException> getExceptionById(@PathVariable Long id) {
        PackageException exception = exceptionService.getExceptionById(id);
        return ResponseEntity.ok(exception);
    }

    @GetMapping("/pending")
    public ResponseEntity<List<PackageException>> getPendingExceptions() {
        List<PackageException> exceptions = exceptionService.getPendingExceptions();
        return ResponseEntity.ok(exceptions);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<PackageException> updateExceptionStatus(
            @PathVariable Long id,
            @RequestBody PackageExceptionUpdateRequest request) {
        PackageException exception = exceptionService.updateExceptionStatus(id, request);
        return ResponseEntity.ok(exception);
    }

    @PostMapping("/compensate")
    public ResponseEntity<PackageException> compensate(@RequestBody CompensationRequest request) {
        PackageException exception = exceptionService.compensate(request);
        return ResponseEntity.ok(exception);
    }

    @GetMapping("/compensations")
    public ResponseEntity<List<PackageException>> getCompensationList() {
        List<PackageException> exceptions = exceptionService.getCompensationList();
        return ResponseEntity.ok(exceptions);
    }
}

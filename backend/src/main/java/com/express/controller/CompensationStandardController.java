package com.express.controller;

import com.express.dto.CompensationStandardRequest;
import com.express.entity.CompensationStandard;
import com.express.entity.ExceptionType;
import com.express.service.CompensationStandardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/compensation-standards")
@RequiredArgsConstructor
public class CompensationStandardController {

    private final CompensationStandardService standardService;

    @PostMapping
    public ResponseEntity<CompensationStandard> createStandard(@RequestBody CompensationStandardRequest request) {
        CompensationStandard standard = standardService.createStandard(request);
        return new ResponseEntity<>(standard, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<CompensationStandard>> getAllStandards() {
        List<CompensationStandard> standards = standardService.getAllStandards();
        return ResponseEntity.ok(standards);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CompensationStandard> getStandardById(@PathVariable Long id) {
        CompensationStandard standard = standardService.getStandardById(id);
        return ResponseEntity.ok(standard);
    }

    @GetMapping("/type/{exceptionType}")
    public ResponseEntity<CompensationStandard> getStandardByType(@PathVariable ExceptionType exceptionType) {
        CompensationStandard standard = standardService.getStandardByType(exceptionType);
        if (standard == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(standard);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CompensationStandard> updateStandard(
            @PathVariable Long id,
            @RequestBody CompensationStandardRequest request) {
        CompensationStandard standard = standardService.updateStandard(id, request);
        return ResponseEntity.ok(standard);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStandard(@PathVariable Long id) {
        standardService.deleteStandard(id);
        return ResponseEntity.noContent().build();
    }
}

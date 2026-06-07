package com.express.controller;

import com.express.dto.*;
import com.express.entity.*;
import com.express.service.PricingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pricing")
@RequiredArgsConstructor
public class PricingController {

    private final PricingService pricingService;

    @PostMapping("/calculate")
    public ResponseEntity<PriceCalculateResponse> calculatePrice(@RequestBody PriceCalculateRequest request) {
        PriceCalculateResponse response = pricingService.calculatePrice(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/companies")
    public ResponseEntity<List<ExpressCompany>> getAllCompanies(
            @RequestParam(required = false) Boolean enabled) {
        List<ExpressCompany> companies = pricingService.getAllCompanies(enabled);
        return ResponseEntity.ok(companies);
    }

    @GetMapping("/companies/{id}")
    public ResponseEntity<ExpressCompany> getCompanyById(@PathVariable Long id) {
        ExpressCompany company = pricingService.getCompanyById(id);
        return ResponseEntity.ok(company);
    }

    @PostMapping("/companies")
    public ResponseEntity<ExpressCompany> createCompany(@RequestBody ExpressCompanyRequest request) {
        ExpressCompany company = pricingService.createCompany(request);
        return new ResponseEntity<>(company, HttpStatus.CREATED);
    }

    @PutMapping("/companies/{id}")
    public ResponseEntity<ExpressCompany> updateCompany(
            @PathVariable Long id,
            @RequestBody ExpressCompanyRequest request) {
        ExpressCompany company = pricingService.updateCompany(id, request);
        return ResponseEntity.ok(company);
    }

    @PutMapping("/companies/{id}/toggle")
    public ResponseEntity<Void> toggleCompany(
            @PathVariable Long id,
            @RequestParam boolean enabled) {
        pricingService.toggleCompany(id, enabled);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/templates")
    public ResponseEntity<List<PriceTemplate>> getTemplates(
            @RequestParam(required = false) Long companyId,
            @RequestParam(required = false) Boolean enabled) {
        List<PriceTemplate> templates = pricingService.getTemplatesByCompany(companyId, enabled);
        return ResponseEntity.ok(templates);
    }

    @GetMapping("/templates/{id}")
    public ResponseEntity<PriceTemplate> getTemplateById(@PathVariable Long id) {
        PriceTemplate template = pricingService.getTemplateById(id);
        return ResponseEntity.ok(template);
    }

    @PostMapping("/templates")
    public ResponseEntity<PriceTemplate> createTemplate(@RequestBody PriceTemplateRequest request) {
        PriceTemplate template = pricingService.createTemplate(request);
        return new ResponseEntity<>(template, HttpStatus.CREATED);
    }

    @PutMapping("/templates/{id}")
    public ResponseEntity<PriceTemplate> updateTemplate(
            @PathVariable Long id,
            @RequestBody PriceTemplateRequest request) {
        PriceTemplate template = pricingService.updateTemplate(id, request);
        return ResponseEntity.ok(template);
    }

    @PutMapping("/templates/{id}/toggle")
    public ResponseEntity<Void> toggleTemplate(
            @PathVariable Long id,
            @RequestParam boolean enabled) {
        pricingService.toggleTemplate(id, enabled);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/templates/{id}/default")
    public ResponseEntity<Void> setDefaultTemplate(@PathVariable Long id) {
        pricingService.setDefaultTemplate(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/rules")
    public ResponseEntity<List<PriceRule>> getRules(
            @RequestParam Long templateId,
            @RequestParam(required = false) Boolean enabled) {
        List<PriceRule> rules = pricingService.getRulesByTemplate(templateId, enabled);
        return ResponseEntity.ok(rules);
    }

    @GetMapping("/rules/{id}")
    public ResponseEntity<PriceRule> getRuleById(@PathVariable Long id) {
        PriceRule rule = pricingService.getRuleById(id);
        return ResponseEntity.ok(rule);
    }

    @PostMapping("/rules")
    public ResponseEntity<PriceRule> createRule(@RequestBody PriceRuleRequest request) {
        PriceRule rule = pricingService.createRule(request);
        return new ResponseEntity<>(rule, HttpStatus.CREATED);
    }

    @PutMapping("/rules/{id}")
    public ResponseEntity<PriceRule> updateRule(
            @PathVariable Long id,
            @RequestBody PriceRuleRequest request) {
        PriceRule rule = pricingService.updateRule(id, request);
        return ResponseEntity.ok(rule);
    }

    @PutMapping("/rules/{id}/toggle")
    public ResponseEntity<Void> toggleRule(
            @PathVariable Long id,
            @RequestParam boolean enabled) {
        pricingService.toggleRule(id, enabled);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/rules/{id}")
    public ResponseEntity<Void> deleteRule(@PathVariable Long id) {
        pricingService.deleteRule(id);
        return ResponseEntity.ok().build();
    }
}

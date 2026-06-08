package com.express.service;

import com.express.dto.*;
import com.express.entity.*;
import com.express.repository.*;
import javax.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PricingService {

    private final ExpressCompanyRepository companyRepository;
    private final PriceTemplateRepository templateRepository;
    private final PriceRuleRepository ruleRepository;

    public PriceCalculateResponse calculatePrice(PriceCalculateRequest request) {
        if (request.getWeight() == null || request.getWeight() <= 0) {
            throw new IllegalArgumentException("重量必须大于0");
        }

        PriceTemplate template;
        if (request.getTemplateId() != null) {
            template = templateRepository.findById(request.getTemplateId())
                    .orElseThrow(() -> new EntityNotFoundException("价格模板不存在: " + request.getTemplateId()));
            if (!template.getEnabled()) {
                throw new IllegalStateException("该价格模板已停用");
            }
        } else if (request.getCompanyId() != null) {
            template = templateRepository.findByCompanyIdAndIsDefaultTrueAndEnabledTrue(request.getCompanyId())
                    .orElseThrow(() -> new EntityNotFoundException("该快递公司没有启用的默认价格模板"));
        } else {
            throw new IllegalArgumentException("必须指定快递公司或价格模板");
        }

        ExpressCompany company = template.getCompany();
        if (!company.getEnabled()) {
            throw new IllegalStateException("该快递公司已停用");
        }

        PriceRule rule = ruleRepository.findMatchingRule(template.getId(), request.getWeight())
                .orElseThrow(() -> new EntityNotFoundException("没有匹配的价格规则，请检查重量范围"));

        BigDecimal basePrice;
        if (rule.getPricePerKg() != null && rule.getPricePerKg().compareTo(BigDecimal.ZERO) > 0) {
            basePrice = rule.getPricePerKg().multiply(BigDecimal.valueOf(request.getWeight()))
                    .setScale(2, RoundingMode.HALF_UP);
        } else {
            basePrice = rule.getBasePrice();
        }

        BigDecimal additionalPrice = BigDecimal.ZERO;

        if (rule.getAdditionalPrice() != null && rule.getAdditionalWeightStep() != null
                && rule.getAdditionalWeightStep() > 0) {
            double baseWeight = rule.getMinWeight();
            double extraWeight = request.getWeight() - baseWeight;
            if (extraWeight > 0) {
                long steps = (long) Math.ceil(extraWeight / rule.getAdditionalWeightStep());
                additionalPrice = rule.getAdditionalPrice().multiply(BigDecimal.valueOf(steps));
            }
        }

        BigDecimal totalPrice = basePrice.add(additionalPrice).setScale(2, RoundingMode.HALF_UP);

        BigDecimal originalPrice = totalPrice;
        if (rule.getOriginalPrice() != null && rule.getOriginalPrice().compareTo(BigDecimal.ZERO) > 0) {
            originalPrice = rule.getOriginalPrice().add(additionalPrice).setScale(2, RoundingMode.HALF_UP);
        }

        return new PriceCalculateResponse(
                totalPrice,
                originalPrice,
                basePrice,
                additionalPrice,
                request.getWeight(),
                company.getName(),
                template.getName()
        );
    }

    public List<ExpressCompany> getAllCompanies(Boolean enabled) {
        if (enabled != null) {
            return enabled ? companyRepository.findByEnabledTrue() : companyRepository.findAll();
        }
        return companyRepository.findAll();
    }

    public ExpressCompany getCompanyById(Long id) {
        return companyRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("快递公司不存在: " + id));
    }

    @Transactional
    public ExpressCompany createCompany(ExpressCompanyRequest request) {
        if (companyRepository.existsByName(request.getName())) {
            throw new IllegalArgumentException("快递公司名称已存在: " + request.getName());
        }
        ExpressCompany company = new ExpressCompany();
        company.setName(request.getName());
        company.setCode(request.getCode());
        company.setContactPhone(request.getContactPhone());
        company.setEnabled(request.getEnabled() != null ? request.getEnabled() : true);
        return companyRepository.save(company);
    }

    @Transactional
    public ExpressCompany updateCompany(Long id, ExpressCompanyRequest request) {
        ExpressCompany company = getCompanyById(id);
        if (request.getName() != null && !request.getName().equals(company.getName())) {
            if (companyRepository.existsByName(request.getName())) {
                throw new IllegalArgumentException("快递公司名称已存在: " + request.getName());
            }
            company.setName(request.getName());
        }
        if (request.getCode() != null) company.setCode(request.getCode());
        if (request.getContactPhone() != null) company.setContactPhone(request.getContactPhone());
        if (request.getEnabled() != null) company.setEnabled(request.getEnabled());
        return companyRepository.save(company);
    }

    @Transactional
    public void toggleCompany(Long id, boolean enabled) {
        ExpressCompany company = getCompanyById(id);
        company.setEnabled(enabled);
        companyRepository.save(company);
    }

    public List<PriceTemplate> getTemplatesByCompany(Long companyId, Boolean enabled) {
        if (companyId != null) {
            if (enabled != null && enabled) {
                return templateRepository.findByCompanyIdAndEnabledTrue(companyId);
            }
            return templateRepository.findAll().stream()
                    .filter(t -> t.getCompany().getId().equals(companyId))
                    .collect(Collectors.toList());
        }
        if (enabled != null && enabled) {
            return templateRepository.findByEnabledTrue();
        }
        return templateRepository.findAll();
    }

    public PriceTemplate getTemplateById(Long id) {
        return templateRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("价格模板不存在: " + id));
    }

    @Transactional
    public PriceTemplate createTemplate(PriceTemplateRequest request) {
        ExpressCompany company = getCompanyById(request.getCompanyId());

        if (templateRepository.existsByNameAndCompanyId(request.getName(), request.getCompanyId())) {
            throw new IllegalArgumentException("该快递公司下已存在同名模板: " + request.getName());
        }

        PriceTemplate template = new PriceTemplate();
        template.setName(request.getName());
        template.setDescription(request.getDescription());
        template.setCompany(company);
        template.setIsDefault(request.getIsDefault() != null ? request.getIsDefault() : false);
        template.setEnabled(request.getEnabled() != null ? request.getEnabled() : true);

        if (Boolean.TRUE.equals(template.getIsDefault())) {
            templateRepository.clearDefaultForCompany(company.getId());
        }

        return templateRepository.save(template);
    }

    @Transactional
    public PriceTemplate updateTemplate(Long id, PriceTemplateRequest request) {
        PriceTemplate template = getTemplateById(id);

        if (request.getName() != null && !request.getName().equals(template.getName())) {
            if (templateRepository.existsByNameAndCompanyId(request.getName(), template.getCompany().getId())) {
                throw new IllegalArgumentException("该快递公司下已存在同名模板: " + request.getName());
            }
            template.setName(request.getName());
        }
        if (request.getDescription() != null) template.setDescription(request.getDescription());

        if (request.getIsDefault() != null) {
            if (Boolean.TRUE.equals(request.getIsDefault()) && !Boolean.TRUE.equals(template.getIsDefault())) {
                templateRepository.clearDefaultForCompany(template.getCompany().getId());
            }
            template.setIsDefault(request.getIsDefault());
        }
        if (request.getEnabled() != null) template.setEnabled(request.getEnabled());

        return templateRepository.save(template);
    }

    @Transactional
    public void toggleTemplate(Long id, boolean enabled) {
        PriceTemplate template = getTemplateById(id);
        template.setEnabled(enabled);
        templateRepository.save(template);
    }

    @Transactional
    public void setDefaultTemplate(Long id) {
        PriceTemplate template = getTemplateById(id);
        templateRepository.clearDefaultForCompany(template.getCompany().getId());
        template.setIsDefault(true);
        templateRepository.save(template);
    }

    public List<PriceRule> getRulesByTemplate(Long templateId, Boolean enabled) {
        if (enabled != null && enabled) {
            return ruleRepository.findByTemplateIdAndEnabledTrueOrderByMinWeightAsc(templateId);
        }
        return ruleRepository.findByTemplateIdOrderByMinWeightAsc(templateId);
    }

    public PriceRule getRuleById(Long id) {
        return ruleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("价格规则不存在: " + id));
    }

    @Transactional
    public PriceRule createRule(PriceRuleRequest request) {
        PriceTemplate template = getTemplateById(request.getTemplateId());

        PriceRule rule = new PriceRule();
        rule.setTemplate(template);
        rule.setMinWeight(request.getMinWeight());
        rule.setMaxWeight(request.getMaxWeight());
        rule.setBasePrice(request.getBasePrice());
        rule.setOriginalPrice(request.getOriginalPrice());
        rule.setPricePerKg(request.getPricePerKg());
        rule.setAdditionalPrice(request.getAdditionalPrice());
        rule.setAdditionalWeightStep(request.getAdditionalWeightStep());
        rule.setEnabled(request.getEnabled() != null ? request.getEnabled() : true);

        return ruleRepository.save(rule);
    }

    @Transactional
    public PriceRule updateRule(Long id, PriceRuleRequest request) {
        PriceRule rule = getRuleById(id);

        if (request.getMinWeight() != null) rule.setMinWeight(request.getMinWeight());
        if (request.getMaxWeight() != null) rule.setMaxWeight(request.getMaxWeight());
        if (request.getBasePrice() != null) rule.setBasePrice(request.getBasePrice());
        if (request.getOriginalPrice() != null) rule.setOriginalPrice(request.getOriginalPrice());
        if (request.getPricePerKg() != null) rule.setPricePerKg(request.getPricePerKg());
        if (request.getAdditionalPrice() != null) rule.setAdditionalPrice(request.getAdditionalPrice());
        if (request.getAdditionalWeightStep() != null) rule.setAdditionalWeightStep(request.getAdditionalWeightStep());
        if (request.getEnabled() != null) rule.setEnabled(request.getEnabled());

        return ruleRepository.save(rule);
    }

    @Transactional
    public void toggleRule(Long id, boolean enabled) {
        PriceRule rule = getRuleById(id);
        rule.setEnabled(enabled);
        ruleRepository.save(rule);
    }

    @Transactional
    public void deleteRule(Long id) {
        if (!ruleRepository.existsById(id)) {
            throw new EntityNotFoundException("价格规则不存在: " + id);
        }
        ruleRepository.deleteById(id);
    }
}

package com.express.config;

import com.express.entity.ExpressCompany;
import com.express.repository.ExpressCompanyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final ExpressCompanyRepository expressCompanyRepository;

    private static final List<String[]> DEFAULT_EXPRESS_COMPANIES = Arrays.asList(
            new String[]{"顺丰速运", "SF", "95338"},
            new String[]{"中通快递", "ZTO", "95311"},
            new String[]{"圆通速递", "YTO", "95554"},
            new String[]{"韵达快递", "YUNDA", "95546"},
            new String[]{"申通快递", "STO", "95543"},
            new String[]{"百世快递", "BEST", "95320"},
            new String[]{"极兔速递", "JTSD", "956025"},
            new String[]{"邮政EMS", "EMS", "11183"},
            new String[]{"京东快递", "JD", "950616"},
            new String[]{"德邦快递", "DEPPON", "95353"},
            new String[]{"天天快递", " TTKY", "400-188-8888"},
            new String[]{"丰巢快递", "FC", "400-066-6666"}
    );

    @Override
    public void run(String... args) {
        initExpressCompanies();
    }

    private void initExpressCompanies() {
        for (String[] company : DEFAULT_EXPRESS_COMPANIES) {
            String name = company[0];
            if (!expressCompanyRepository.existsByName(name)) {
                ExpressCompany entity = new ExpressCompany();
                entity.setName(name);
                entity.setCode(company[1]);
                entity.setContactPhone(company[2]);
                entity.setEnabled(true);
                expressCompanyRepository.save(entity);
                log.info("初始化快递公司: {}", name);
            }
        }
        log.info("快递公司初始化完成，共 {} 家", expressCompanyRepository.count());
    }
}

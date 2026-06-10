package com.school.fees.service;

import com.school.fees.config.SchoolClassCatalog;
import com.school.fees.entity.ClassFeeConfig;
import com.school.fees.repository.ClassFeeConfigRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ClassFeeConfigService {

    private final ClassFeeConfigRepository classFeeConfigRepository;

    public ClassFeeConfigService(ClassFeeConfigRepository classFeeConfigRepository) {
        this.classFeeConfigRepository = classFeeConfigRepository;
    }

    @Transactional(readOnly = true)
    public List<ClassFeeConfig> findAll() {
        return classFeeConfigRepository.findAllByOrderByIdAsc();
    }

    @Transactional(readOnly = true)
    public Map<String, BigDecimal> feeMap() {
        Map<String, BigDecimal> fees = new LinkedHashMap<>();
        SchoolClassCatalog.CLASS_OPTIONS.forEach(className -> fees.put(className, BigDecimal.ZERO.setScale(2)));
        classFeeConfigRepository.findAll().forEach(config ->
                fees.put(config.getClassName(), money(config.getFeeAmount())));
        return fees;
    }

    @Transactional
    public void updateFees(Map<String, String> submittedFees) {
        for (String className : SchoolClassCatalog.CLASS_OPTIONS) {
            String value = submittedFees.get("fee_" + SchoolClassCatalog.CLASS_OPTIONS.indexOf(className));
            BigDecimal fee = parseMoney(value);
            ClassFeeConfig config = classFeeConfigRepository.findByClassName(className)
                    .orElseGet(() -> {
                        ClassFeeConfig created = new ClassFeeConfig();
                        created.setClassName(className);
                        return created;
                    });
            config.setFeeAmount(fee);
            classFeeConfigRepository.save(config);
        }
    }

    private BigDecimal parseMoney(String value) {
        if (value == null || value.isBlank()) {
            return BigDecimal.ZERO.setScale(2);
        }
        return money(new BigDecimal(value.trim()));
    }

    private BigDecimal money(BigDecimal value) {
        return (value == null ? BigDecimal.ZERO : value).setScale(2, RoundingMode.HALF_UP);
    }
}

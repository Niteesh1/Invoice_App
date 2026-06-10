package com.school.fees.repository;

import com.school.fees.entity.ClassFeeConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClassFeeConfigRepository extends JpaRepository<ClassFeeConfig, Long> {

    List<ClassFeeConfig> findAllByOrderByIdAsc();

    Optional<ClassFeeConfig> findByClassName(String className);
}

package com.school.fees.repository;

import com.school.fees.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StudentRepository extends JpaRepository<Student, Long> {

    List<Student> findAllByOrderByNameAsc();

    List<Student> findByNameContainingIgnoreCaseOrGradeContainingIgnoreCaseOrderByNameAsc(String name, String grade);
}

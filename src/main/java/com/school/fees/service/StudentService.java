package com.school.fees.service;

import com.school.fees.entity.Student;
import com.school.fees.exception.ResourceNotFoundException;
import com.school.fees.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class StudentService {

    private final StudentRepository studentRepository;

    public StudentService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    @Transactional(readOnly = true)
    public List<Student> findAll(String search) {
        if (!StringUtils.hasText(search)) {
            return studentRepository.findAllByOrderByNameAsc();
        }
        String term = search.trim();
        return studentRepository.findByNameContainingIgnoreCaseOrGradeContainingIgnoreCaseOrderByNameAsc(term, term);
    }

    @Transactional(readOnly = true)
    public List<Student> findAll() {
        return studentRepository.findAllByOrderByNameAsc();
    }

    @Transactional(readOnly = true)
    public Student get(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
    }

    @Transactional
    public Student save(Student student) {
        normalize(student);
        return studentRepository.save(student);
    }

    private void normalize(Student student) {
        student.setName(trim(student.getName()));
        student.setGrade(trim(student.getGrade()));
        student.setContactNumber(trim(student.getContactNumber()));
        student.setAddress(trim(student.getAddress()));
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }
}

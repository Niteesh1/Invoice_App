package com.school.fees.service;

import com.school.fees.entity.Student;
import com.school.fees.exception.ResourceNotFoundException;
import com.school.fees.repository.BookIssueRepository;
import com.school.fees.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class StudentService {

    private final StudentRepository studentRepository;
    private final BookIssueRepository bookIssueRepository;

    public StudentService(StudentRepository studentRepository, BookIssueRepository bookIssueRepository) {
        this.studentRepository = studentRepository;
        this.bookIssueRepository = bookIssueRepository;
    }

    @Transactional(readOnly = true)
    public List<Student> findAll(String search) {
        return findAll(search, null);
    }

    @Transactional(readOnly = true)
    public List<Student> findAll(String search, String grade) {
        return findAll().stream()
                .filter(student -> matchesGrade(student, grade))
                .filter(student -> matchesSearch(student, search))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Student> findAll() {
        return studentRepository.findAllByOrderByNameAsc();
    }

    private boolean matchesSearch(Student student, String search) {
        if (!StringUtils.hasText(search)) {
            return true;
        }
        String term = search.trim().toLowerCase();
        return student.getName().toLowerCase().contains(term)
                || (student.getContactNumber() != null && student.getContactNumber().toLowerCase().contains(term));
    }

    private boolean matchesGrade(Student student, String grade) {
        return !StringUtils.hasText(grade) || student.getGrade().equals(grade);
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

    @Transactional
    public Student update(Long id, Student updatedStudent) {
        Student existing = get(id);
        existing.setName(updatedStudent.getName());
        existing.setGrade(updatedStudent.getGrade());
        existing.setContactNumber(updatedStudent.getContactNumber());
        existing.setAddress(updatedStudent.getAddress());
        normalize(existing);
        return studentRepository.save(existing);
    }

    @Transactional
    public String deleteEntireRecord(Long id) {
        Student student = get(id);
        var issues = bookIssueRepository.findByStudentIdOrderByIssueDateDescIdDesc(id);
        bookIssueRepository.deleteAll(issues);
        bookIssueRepository.flush();
        studentRepository.delete(student);
        return student.getName();
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

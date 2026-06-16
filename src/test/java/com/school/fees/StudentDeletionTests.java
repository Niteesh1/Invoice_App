package com.school.fees;

import com.school.fees.dto.IssueForm;
import com.school.fees.dto.PaymentForm;
import com.school.fees.entity.Student;
import com.school.fees.repository.BookIssueRepository;
import com.school.fees.repository.PaymentRepository;
import com.school.fees.repository.StudentRepository;
import com.school.fees.service.BookIssueService;
import com.school.fees.service.PaymentService;
import com.school.fees.service.StudentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
@Transactional
class StudentDeletionTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private StudentService studentService;

    @Autowired
    private BookIssueService bookIssueService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private BookIssueRepository bookIssueRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Test
    void adminCanDeleteStudentAndAllRelatedData() throws Exception {
        Student student = createStudent("Incorrect Student");
        var issue = createIssue(student);
        createPayment(issue.getId());

        mockMvc.perform(post("/students/{id}/delete", student.getId())
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/students"));

        assertThat(studentRepository.findById(student.getId())).isEmpty();
        assertThat(bookIssueRepository.findByStudentIdOrderByIssueDateDescIdDesc(student.getId())).isEmpty();
        assertThat(paymentRepository.findByBookIssueStudentIdOrderByPaymentDateDescIdDesc(student.getId())).isEmpty();
    }

    @Test
    void cashierCannotDeleteEntireStudentRecord() throws Exception {
        Student student = createStudent("Protected Student");

        mockMvc.perform(post("/students/{id}/delete", student.getId())
                        .with(user("cashier").roles("CASHIER"))
                        .with(csrf()))
                .andExpect(status().isForbidden());

        assertThat(studentRepository.findById(student.getId())).isPresent();
    }

    @Test
    void deleteEntireRecordButtonIsVisibleOnlyToAdmin() throws Exception {
        Student student = createStudent("Visible Student");

        mockMvc.perform(get("/students/{id}", student.getId())
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Delete Entire Record")));

        mockMvc.perform(get("/students/{id}", student.getId())
                        .with(user("cashier").roles("CASHIER")))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.not(
                        org.hamcrest.Matchers.containsString("Delete Entire Record")
                )));
    }

    private Student createStudent(String name) {
        Student student = new Student();
        student.setName(name);
        student.setGrade("Class 1");
        return studentService.save(student);
    }

    private com.school.fees.entity.BookIssue createIssue(Student student) {
        IssueForm form = new IssueForm();
        form.setStudentId(student.getId());
        form.setSelectedClass(student.getGrade());
        form.setIssueDate(LocalDate.now());
        form.setBookTitle("Test Book Set");
        form.setQuantity(1);
        form.setUnitPrice(new BigDecimal("100.00"));
        form.setDiscount(BigDecimal.ZERO);
        return bookIssueService.create(form);
    }

    private void createPayment(Long issueId) {
        PaymentForm form = new PaymentForm();
        form.setBookIssueId(issueId);
        form.setPaymentDate(LocalDate.now());
        form.setAmount(new BigDecimal("50.00"));
        paymentService.record(form, "admin");
    }
}

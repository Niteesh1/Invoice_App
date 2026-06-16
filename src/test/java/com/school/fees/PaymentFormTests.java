package com.school.fees;

import com.school.fees.dto.IssueForm;
import com.school.fees.entity.Student;
import com.school.fees.service.BookIssueService;
import com.school.fees.service.StudentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
@Transactional
class PaymentFormTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private StudentService studentService;

    @Autowired
    private BookIssueService bookIssueService;

    @Test
    void newPaymentDefaultsToBookIssueDate() throws Exception {
        LocalDate issueDate = LocalDate.of(2026, 6, 10);
        var issue = createIssue(issueDate);

        mockMvc.perform(get("/payments/new")
                .param("issueId", issue.getId().toString())
                        .with(user("cashier").roles("CASHIER")))
                .andExpect(status().isOk())
                .andExpect(model().attribute(
                        "paymentForm",
                        hasProperty("paymentDate", equalTo(issueDate))
                ));
    }

    private com.school.fees.entity.BookIssue createIssue(LocalDate issueDate) {
        Student student = new Student();
        student.setName("Payment Date Student");
        student.setGrade("Class 1");
        Student savedStudent = studentService.save(student);

        IssueForm form = new IssueForm();
        form.setStudentId(savedStudent.getId());
        form.setSelectedClass(savedStudent.getGrade());
        form.setIssueDate(issueDate);
        form.setBookTitle("Test Book Set");
        form.setQuantity(1);
        form.setUnitPrice(new BigDecimal("100.00"));
        form.setDiscount(BigDecimal.ZERO);
        return bookIssueService.create(form);
    }
}

package com.school.fees.service;

import com.school.fees.dto.PaymentForm;
import com.school.fees.entity.BookIssue;
import com.school.fees.entity.Payment;
import com.school.fees.exception.BusinessRuleException;
import com.school.fees.exception.ResourceNotFoundException;
import com.school.fees.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class PaymentService {

    private static final DateTimeFormatter RECEIPT_DATE_FORMAT = DateTimeFormatter.BASIC_ISO_DATE;

    private final PaymentRepository paymentRepository;
    private final BookIssueService bookIssueService;

    public PaymentService(PaymentRepository paymentRepository, BookIssueService bookIssueService) {
        this.paymentRepository = paymentRepository;
        this.bookIssueService = bookIssueService;
    }

    @Transactional(readOnly = true)
    public List<Payment> findAll() {
        return paymentRepository.findAllByOrderByPaymentDateDescIdDesc();
    }

    @Transactional(readOnly = true)
    public List<Payment> findBetween(LocalDate startDate, LocalDate endDate) {
        return paymentRepository.findByPaymentDateBetweenOrderByPaymentDateDescIdDesc(startDate, endDate);
    }

    @Transactional(readOnly = true)
    public List<Payment> findForStudent(Long studentId) {
        return paymentRepository.findByBookIssueStudentIdOrderByPaymentDateDescIdDesc(studentId);
    }

    @Transactional(readOnly = true)
    public Payment get(Long id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found"));
    }

    @Transactional(readOnly = true)
    public Payment getByReceiptNumber(String receiptNumber) {
        return paymentRepository.findByReceiptNumber(receiptNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Receipt not found"));
    }

    @Transactional
    public Payment record(PaymentForm form, String createdBy) {
        BookIssue issue = bookIssueService.getDetailed(form.getBookIssueId());
        BigDecimal amount = money(form.getAmount());
        if (amount.compareTo(issue.getBalance()) > 0) {
            throw new BusinessRuleException("Payment cannot exceed pending balance");
        }

        Payment payment = new Payment();
        payment.setBookIssue(issue);
        payment.setReceiptNumber(nextReceiptNumber(form.getPaymentDate()));
        payment.setPaymentDate(form.getPaymentDate());
        payment.setAmount(amount);
        payment.setPaymentMode(form.getPaymentMode());
        payment.setNotes(trim(form.getNotes()));
        payment.setCreatedBy(createdBy);

        Payment saved = paymentRepository.save(payment);
        issue.getPayments().add(saved);
        bookIssueService.refreshStatus(issue);
        return saved;
    }

    @Transactional(readOnly = true)
    public BigDecimal totalCollected() {
        return paymentRepository.sumCollected();
    }

    @Transactional(readOnly = true)
    public BigDecimal collectedOn(LocalDate date) {
        return paymentRepository.sumCollectedOn(date);
    }

    @Transactional(readOnly = true)
    public BigDecimal collectedBetween(LocalDate startDate, LocalDate endDate) {
        return paymentRepository.sumCollectedBetween(startDate, endDate);
    }

    private String nextReceiptNumber(LocalDate paymentDate) {
        long count = paymentRepository.countByPaymentDate(paymentDate) + 1;
        String base = "R" + paymentDate.format(RECEIPT_DATE_FORMAT);
        String receiptNumber = base + "-" + String.format("%04d", count);
        while (paymentRepository.findByReceiptNumber(receiptNumber).isPresent()) {
            count++;
            receiptNumber = base + "-" + String.format("%04d", count);
        }
        return receiptNumber;
    }

    private BigDecimal money(BigDecimal value) {
        return (value == null ? BigDecimal.ZERO : value).setScale(2, RoundingMode.HALF_UP);
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }
}

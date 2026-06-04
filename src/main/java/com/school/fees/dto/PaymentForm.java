package com.school.fees.dto;

import com.school.fees.entity.PaymentMode;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public class PaymentForm {

    @NotNull
    private Long bookIssueId;

    @NotNull
    private LocalDate paymentDate = LocalDate.now();

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal amount;

    @NotNull
    private PaymentMode paymentMode = PaymentMode.CASH;

    @Size(max = 255)
    private String notes;

    public Long getBookIssueId() {
        return bookIssueId;
    }

    public void setBookIssueId(Long bookIssueId) {
        this.bookIssueId = bookIssueId;
    }

    public LocalDate getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDate paymentDate) {
        this.paymentDate = paymentDate;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public PaymentMode getPaymentMode() {
        return paymentMode;
    }

    public void setPaymentMode(PaymentMode paymentMode) {
        this.paymentMode = paymentMode;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}

package com.school.fees.repository;

import com.school.fees.entity.Payment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    @Override
    @EntityGraph(attributePaths = {"bookIssue", "bookIssue.student", "bookIssue.payments"})
    Optional<Payment> findById(Long id);

    @EntityGraph(attributePaths = {"bookIssue", "bookIssue.student"})
    List<Payment> findAllByOrderByPaymentDateDescIdDesc();

    @EntityGraph(attributePaths = {"bookIssue", "bookIssue.student"})
    List<Payment> findByPaymentDateBetweenOrderByPaymentDateDescIdDesc(LocalDate startDate, LocalDate endDate);

    @EntityGraph(attributePaths = {"bookIssue", "bookIssue.student"})
    List<Payment> findByBookIssueStudentIdOrderByPaymentDateDescIdDesc(Long studentId);

    @EntityGraph(attributePaths = {"bookIssue", "bookIssue.student", "bookIssue.payments"})
    Optional<Payment> findByReceiptNumber(String receiptNumber);

    long countByPaymentDate(LocalDate paymentDate);

    @Query("select coalesce(sum(p.amount), 0) from Payment p")
    BigDecimal sumCollected();

    @Query("select coalesce(sum(p.amount), 0) from Payment p where p.paymentDate = :paymentDate")
    BigDecimal sumCollectedOn(@Param("paymentDate") LocalDate paymentDate);

    @Query("select coalesce(sum(p.amount), 0) from Payment p where p.paymentDate between :startDate and :endDate")
    BigDecimal sumCollectedBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}

package com.school.fees.repository;

import com.school.fees.entity.BookIssue;
import com.school.fees.entity.IssueStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface BookIssueRepository extends JpaRepository<BookIssue, Long> {

    @EntityGraph(attributePaths = {"student", "payments"})
    List<BookIssue> findAllByOrderByIssueDateDescIdDesc();

    @EntityGraph(attributePaths = {"student", "payments"})
    List<BookIssue> findByStatusOrderByIssueDateDescIdDesc(IssueStatus status);

    @EntityGraph(attributePaths = {"student", "payments"})
    List<BookIssue> findByIssueDateBetweenOrderByIssueDateDescIdDesc(LocalDate startDate, LocalDate endDate);

    @EntityGraph(attributePaths = {"student", "payments"})
    List<BookIssue> findByStudentIdOrderByIssueDateDescIdDesc(Long studentId);

    @EntityGraph(attributePaths = {"student", "payments"})
    @Query("select i from BookIssue i where i.id = :id")
    Optional<BookIssue> findDetailedById(@Param("id") Long id);

    long countByStatus(IssueStatus status);

    @Query("select coalesce(sum(i.totalDue), 0) from BookIssue i")
    BigDecimal sumTotalDue();

    @Query("select coalesce(sum(i.totalDue), 0) from BookIssue i where i.issueDate between :startDate and :endDate")
    BigDecimal sumTotalDueBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}

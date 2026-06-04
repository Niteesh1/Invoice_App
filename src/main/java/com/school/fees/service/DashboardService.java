package com.school.fees.service;

import com.school.fees.dto.DashboardStats;
import com.school.fees.entity.BookIssue;
import com.school.fees.entity.IssueStatus;
import com.school.fees.repository.BookIssueRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
public class DashboardService {

    private final BookIssueRepository bookIssueRepository;
    private final BookIssueService bookIssueService;
    private final PaymentService paymentService;

    public DashboardService(
            BookIssueRepository bookIssueRepository,
            BookIssueService bookIssueService,
            PaymentService paymentService
    ) {
        this.bookIssueRepository = bookIssueRepository;
        this.bookIssueService = bookIssueService;
        this.paymentService = paymentService;
    }

    @Transactional(readOnly = true)
    public DashboardStats stats() {
        BigDecimal pendingAmount = bookIssueService.findAll().stream()
                .map(BookIssue::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new DashboardStats(
                paymentService.collectedOn(LocalDate.now()),
                paymentService.totalCollected(),
                pendingAmount,
                bookIssueRepository.countByStatus(IssueStatus.PENDING),
                bookIssueRepository.countByStatus(IssueStatus.PARTIAL),
                bookIssueRepository.countByStatus(IssueStatus.PAID)
        );
    }
}

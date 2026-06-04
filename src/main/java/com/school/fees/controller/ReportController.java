package com.school.fees.controller;

import com.school.fees.entity.IssueStatus;
import com.school.fees.service.BookIssueService;
import com.school.fees.service.PaymentService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

@Controller
@RequestMapping("/reports")
public class ReportController {

    private final BookIssueService bookIssueService;
    private final PaymentService paymentService;

    public ReportController(BookIssueService bookIssueService, PaymentService paymentService) {
        this.bookIssueService = bookIssueService;
        this.paymentService = paymentService;
    }

    @GetMapping
    public String index() {
        return "reports/index";
    }

    @GetMapping("/collection")
    public String collection(@RequestParam(required = false) LocalDate startDate,
                             @RequestParam(required = false) LocalDate endDate,
                             Model model) {
        LocalDate start = startDate == null ? LocalDate.now() : startDate;
        LocalDate end = endDate == null ? start : endDate;
        model.addAttribute("startDate", start);
        model.addAttribute("endDate", end);
        model.addAttribute("payments", paymentService.findBetween(start, end));
        model.addAttribute("total", paymentService.collectedBetween(start, end));
        return "reports/collection";
    }

    @GetMapping("/sales")
    public String sales(@RequestParam(required = false) LocalDate startDate,
                        @RequestParam(required = false) LocalDate endDate,
                        Model model) {
        LocalDate start = startDate == null ? LocalDate.now() : startDate;
        LocalDate end = endDate == null ? start : endDate;
        model.addAttribute("startDate", start);
        model.addAttribute("endDate", end);
        model.addAttribute("issues", bookIssueService.findBetween(start, end));
        return "reports/sales";
    }

    @GetMapping("/pending")
    public String pending(Model model) {
        model.addAttribute("pendingIssues", bookIssueService.findByStatus(IssueStatus.PENDING));
        model.addAttribute("partialIssues", bookIssueService.findByStatus(IssueStatus.PARTIAL));
        return "reports/pending";
    }
}

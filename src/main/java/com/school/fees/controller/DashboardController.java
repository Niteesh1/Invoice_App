package com.school.fees.controller;

import com.school.fees.entity.IssueStatus;
import com.school.fees.service.BookIssueService;
import com.school.fees.service.DashboardService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    private final DashboardService dashboardService;
    private final BookIssueService bookIssueService;

    public DashboardController(DashboardService dashboardService, BookIssueService bookIssueService) {
        this.dashboardService = dashboardService;
        this.bookIssueService = bookIssueService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("stats", dashboardService.stats());
        model.addAttribute("oldPendingIssues", bookIssueService.findByStatus(IssueStatus.PENDING).stream().limit(10).toList());
        return "dashboard";
    }
}

package com.school.fees.controller;

import com.school.fees.config.SchoolClassCatalog;
import com.school.fees.dto.IssueForm;
import com.school.fees.entity.IssueStatus;
import com.school.fees.exception.BusinessRuleException;
import com.school.fees.service.BookIssueService;
import com.school.fees.service.StudentService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/fees")
public class FeeController {

    private final BookIssueService bookIssueService;
    private final StudentService studentService;

    public FeeController(BookIssueService bookIssueService, StudentService studentService) {
        this.bookIssueService = bookIssueService;
        this.studentService = studentService;
    }

    @GetMapping
    public String list(@RequestParam(required = false) IssueStatus status,
                       @RequestParam(required = false) String search,
                       Model model) {
        model.addAttribute("issues", bookIssueService.findByStatus(status).stream()
                .filter(issue -> bookIssueService.matchesSearch(issue, search))
                .toList());
        model.addAttribute("statuses", IssueStatus.values());
        model.addAttribute("selectedStatus", status);
        model.addAttribute("search", search);
        return "fees/list";
    }

    @GetMapping("/new")
    public String newIssue(Model model) {
        prepareForm(model, new IssueForm());
        return "fees/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("issueForm") IssueForm issueForm,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            prepareForm(model, issueForm);
            return "fees/form";
        }
        try {
            var issue = bookIssueService.create(issueForm);
            redirectAttributes.addFlashAttribute("message", "Book issue saved");
            return "redirect:/fees/" + issue.getId();
        } catch (BusinessRuleException ex) {
            bindingResult.reject("businessRule", ex.getMessage());
            prepareForm(model, issueForm);
            return "fees/form";
        }
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("issue", bookIssueService.getDetailed(id));
        return "fees/detail";
    }

    private void prepareForm(Model model, IssueForm issueForm) {
        model.addAttribute("issueForm", issueForm);
        model.addAttribute("students", studentService.findAll());
        model.addAttribute("classOptions", SchoolClassCatalog.CLASS_OPTIONS);
    }
}

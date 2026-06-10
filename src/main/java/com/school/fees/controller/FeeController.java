package com.school.fees.controller;

import com.school.fees.config.SchoolClassCatalog;
import com.school.fees.dto.IssueForm;
import com.school.fees.entity.IssueStatus;
import com.school.fees.exception.BusinessRuleException;
import com.school.fees.service.BookIssueService;
import com.school.fees.service.ClassFeeConfigService;
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

import java.time.LocalDate;

@Controller
@RequestMapping("/fees")
public class FeeController {

    private final BookIssueService bookIssueService;
    private final StudentService studentService;
    private final ClassFeeConfigService classFeeConfigService;

    public FeeController(BookIssueService bookIssueService,
                         StudentService studentService,
                         ClassFeeConfigService classFeeConfigService) {
        this.bookIssueService = bookIssueService;
        this.studentService = studentService;
        this.classFeeConfigService = classFeeConfigService;
    }

    @GetMapping
    public String list(@RequestParam(required = false) IssueStatus status,
                       @RequestParam(required = false) String search,
                       @RequestParam(required = false) String grade,
                       @RequestParam(required = false) LocalDate issueDate,
                       @RequestParam(required = false, defaultValue = "dateDesc") String sort,
                       Model model) {
        var issues = bookIssueService.findFiltered(status, search, grade, issueDate, sort);
        LocalDate summaryDate = issueDate == null ? LocalDate.now() : issueDate;
        var dayIssues = bookIssueService.findFiltered(status, search, grade, summaryDate, "classAsc");
        model.addAttribute("issues", issues);
        model.addAttribute("listTotals", bookIssueService.totals(issues));
        model.addAttribute("summaryDate", summaryDate);
        model.addAttribute("dayTotals", bookIssueService.totals(dayIssues));
        model.addAttribute("classSummaries", bookIssueService.classSummaries(dayIssues));
        model.addAttribute("statuses", IssueStatus.values());
        model.addAttribute("classOptions", SchoolClassCatalog.CLASS_OPTIONS);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("search", search);
        model.addAttribute("selectedGrade", grade);
        model.addAttribute("issueDate", issueDate);
        model.addAttribute("selectedSort", sort);
        return "fees/list";
    }

    @GetMapping("/new")
    public String newIssue(@RequestParam(required = false) Long studentId, Model model) {
        IssueForm form = new IssueForm();
        form.setIssueDate(LocalDate.now());
        if (studentId != null) {
            var student = studentService.get(studentId);
            form.setStudentId(student.getId());
            form.setSelectedClass(student.getGrade());
            model.addAttribute("preselectedStudent", student);
        }
        prepareForm(model, form);
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

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id,
                         RedirectAttributes redirectAttributes) {
        try {
            bookIssueService.deleteIfNoPayments(id);
            redirectAttributes.addFlashAttribute("message", "Book issue deleted");
            return "redirect:/fees";
        } catch (BusinessRuleException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/fees/" + id;
        }
    }

    private void prepareForm(Model model, IssueForm issueForm) {
        model.addAttribute("issueForm", issueForm);
        model.addAttribute("students", studentService.findAll());
        model.addAttribute("classOptions", SchoolClassCatalog.CLASS_OPTIONS);
        model.addAttribute("classFeeMap", classFeeConfigService.feeMap());
        model.addAttribute("bookTitleSuggestions", java.util.List.of(
                "Textbook & Workbook Set",
                "Workbook Set",
                "Textbook Set",
                "Complete Book Set"
        ));
    }
}

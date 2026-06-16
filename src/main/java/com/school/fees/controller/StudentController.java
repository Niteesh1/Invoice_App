package com.school.fees.controller;

import com.school.fees.config.SchoolClassCatalog;
import com.school.fees.entity.Student;
import com.school.fees.service.BookIssueService;
import com.school.fees.service.PaymentService;
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
@RequestMapping("/students")
public class StudentController {

    private final StudentService studentService;
    private final BookIssueService bookIssueService;
    private final PaymentService paymentService;

    public StudentController(StudentService studentService, BookIssueService bookIssueService, PaymentService paymentService) {
        this.studentService = studentService;
        this.bookIssueService = bookIssueService;
        this.paymentService = paymentService;
    }

    @GetMapping
    public String list(@RequestParam(required = false) String search,
                       @RequestParam(required = false) String grade,
                       Model model) {
        model.addAttribute("students", studentService.findAll(search, grade));
        model.addAttribute("search", search);
        model.addAttribute("selectedGrade", grade);
        model.addAttribute("classOptions", SchoolClassCatalog.CLASS_OPTIONS);
        return "students/list";
    }

    @GetMapping("/new")
    public String newStudent(Model model) {
        model.addAttribute("student", new Student());
        model.addAttribute("formTitle", "Add Student");
        model.addAttribute("formSubtitle", "Student/customer details for fee records and ledger views.");
        model.addAttribute("formAction", "/students");
        model.addAttribute("classOptions", SchoolClassCatalog.CLASS_OPTIONS);
        return "students/form";
    }

    @PostMapping
    public String save(@Valid @ModelAttribute Student student,
                       BindingResult bindingResult,
                       Model model,
                       RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("classOptions", SchoolClassCatalog.CLASS_OPTIONS);
            model.addAttribute("formTitle", "Add Student");
            model.addAttribute("formSubtitle", "Student/customer details for fee records and ledger views.");
            model.addAttribute("formAction", "/students");
            return "students/form";
        }
        Student saved = studentService.save(student);
        redirectAttributes.addFlashAttribute("message", "Student saved");
        return "redirect:/students/" + saved.getId();
    }

    @GetMapping("/{id}/edit")
    public String edit(@PathVariable Long id, Model model) {
        model.addAttribute("student", studentService.get(id));
        model.addAttribute("formTitle", "Edit Student");
        model.addAttribute("formSubtitle", "Correct student name, class, contact, or address.");
        model.addAttribute("formAction", "/students/" + id + "/edit");
        model.addAttribute("classOptions", SchoolClassCatalog.CLASS_OPTIONS);
        return "students/form";
    }

    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute Student student,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("classOptions", SchoolClassCatalog.CLASS_OPTIONS);
            model.addAttribute("formTitle", "Edit Student");
            model.addAttribute("formSubtitle", "Correct student name, class, contact, or address.");
            model.addAttribute("formAction", "/students/" + id + "/edit");
            return "students/form";
        }
        Student saved = studentService.update(id, student);
        redirectAttributes.addFlashAttribute("message", "Student details updated");
        return "redirect:/students/" + saved.getId();
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("student", studentService.get(id));
        model.addAttribute("issues", bookIssueService.findForStudent(id));
        model.addAttribute("payments", paymentService.findForStudent(id));
        return "students/detail";
    }

    @PostMapping("/{id}/delete")
    public String deleteEntireRecord(@PathVariable Long id,
                                     RedirectAttributes redirectAttributes) {
        String studentName = studentService.deleteEntireRecord(id);
        redirectAttributes.addFlashAttribute(
                "message",
                "Deleted " + studentName + " and all related book issues and payments"
        );
        return "redirect:/students";
    }
}

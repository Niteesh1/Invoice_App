package com.school.fees.controller;

import com.school.fees.dto.PaymentForm;
import com.school.fees.entity.PaymentMode;
import com.school.fees.exception.BusinessRuleException;
import com.school.fees.service.BookIssueService;
import com.school.fees.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
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
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentService paymentService;
    private final BookIssueService bookIssueService;

    public PaymentController(PaymentService paymentService, BookIssueService bookIssueService) {
        this.paymentService = paymentService;
        this.bookIssueService = bookIssueService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("payments", paymentService.findAll());
        return "payments/list";
    }

    @GetMapping("/new")
    public String newPayment(@RequestParam Long issueId, Model model) {
        PaymentForm form = new PaymentForm();
        form.setBookIssueId(issueId);
        prepareForm(model, form);
        return "payments/form";
    }

    @PostMapping
    public String record(@Valid @ModelAttribute("paymentForm") PaymentForm paymentForm,
                         BindingResult bindingResult,
                         Authentication authentication,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            prepareForm(model, paymentForm);
            return "payments/form";
        }
        try {
            var payment = paymentService.record(paymentForm, authentication.getName());
            redirectAttributes.addFlashAttribute("message", "Payment recorded");
            return "redirect:/payments/" + payment.getId() + "/receipt";
        } catch (BusinessRuleException ex) {
            bindingResult.reject("businessRule", ex.getMessage());
            prepareForm(model, paymentForm);
            return "payments/form";
        }
    }

    @GetMapping("/{id}/receipt")
    public String receipt(@PathVariable Long id, Model model) {
        model.addAttribute("payment", paymentService.get(id));
        return "payments/receipt";
    }

    private void prepareForm(Model model, PaymentForm paymentForm) {
        model.addAttribute("paymentForm", paymentForm);
        model.addAttribute("issue", bookIssueService.getDetailed(paymentForm.getBookIssueId()));
        model.addAttribute("paymentModes", PaymentMode.values());
    }
}

package com.school.fees.controller;

import com.school.fees.service.ClassFeeConfigService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

@Controller
@RequestMapping("/book-fee-config")
public class ClassFeeConfigController {

    private final ClassFeeConfigService classFeeConfigService;

    public ClassFeeConfigController(ClassFeeConfigService classFeeConfigService) {
        this.classFeeConfigService = classFeeConfigService;
    }

    @GetMapping
    public String index(Model model) {
        model.addAttribute("configs", classFeeConfigService.findAll());
        return "config/book-fees";
    }

    @PostMapping
    public String update(@RequestParam Map<String, String> submittedFees,
                         RedirectAttributes redirectAttributes) {
        classFeeConfigService.updateFees(submittedFees);
        redirectAttributes.addFlashAttribute("message", "Book fee config updated");
        return "redirect:/book-fee-config";
    }
}

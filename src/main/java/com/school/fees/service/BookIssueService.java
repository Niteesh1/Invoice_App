package com.school.fees.service;

import com.school.fees.dto.IssueForm;
import com.school.fees.entity.BookIssue;
import com.school.fees.entity.IssueStatus;
import com.school.fees.entity.Student;
import com.school.fees.exception.BusinessRuleException;
import com.school.fees.exception.ResourceNotFoundException;
import com.school.fees.repository.BookIssueRepository;
import com.school.fees.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class BookIssueService {

    private final BookIssueRepository bookIssueRepository;
    private final StudentRepository studentRepository;

    public BookIssueService(BookIssueRepository bookIssueRepository, StudentRepository studentRepository) {
        this.bookIssueRepository = bookIssueRepository;
        this.studentRepository = studentRepository;
    }

    @Transactional(readOnly = true)
    public List<BookIssue> findAll() {
        return bookIssueRepository.findAllByOrderByIssueDateDescIdDesc();
    }

    @Transactional(readOnly = true)
    public List<BookIssue> findByStatus(IssueStatus status) {
        if (status == null) {
            return findAll();
        }
        return bookIssueRepository.findByStatusOrderByIssueDateDescIdDesc(status);
    }

    @Transactional(readOnly = true)
    public List<BookIssue> findFiltered(IssueStatus status, String search, String grade, LocalDate issueDate, String sort) {
        Comparator<BookIssue> comparator = switch (sort == null ? "" : sort) {
            case "dateAsc" -> Comparator.comparing(BookIssue::getIssueDate).thenComparing(BookIssue::getId);
            case "classAsc" -> Comparator.comparing((BookIssue issue) -> issue.getStudent().getGrade(), String.CASE_INSENSITIVE_ORDER)
                    .thenComparing(issue -> issue.getStudent().getName(), String.CASE_INSENSITIVE_ORDER);
            case "studentAsc" -> Comparator.comparing((BookIssue issue) -> issue.getStudent().getName(), String.CASE_INSENSITIVE_ORDER);
            case "balanceDesc" -> Comparator.comparing(BookIssue::getBalance).reversed();
            case "balanceAsc" -> Comparator.comparing(BookIssue::getBalance);
            default -> Comparator.comparing(BookIssue::getIssueDate).reversed().thenComparing(Comparator.comparing(BookIssue::getId).reversed());
        };

        return findByStatus(status).stream()
                .filter(issue -> matchesSearch(issue, search))
                .filter(issue -> !StringUtils.hasText(grade) || issue.getStudent().getGrade().equals(grade))
                .filter(issue -> issueDate == null || issue.getIssueDate().equals(issueDate))
                .sorted(comparator)
                .toList();
    }

    public IssueTotals totals(List<BookIssue> issues) {
        BigDecimal totalDue = issues.stream()
                .map(BookIssue::getTotalDue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal collected = issues.stream()
                .map(BookIssue::getCollectedAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal balance = issues.stream()
                .map(BookIssue::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new IssueTotals(issues.size(), money(totalDue), money(collected), money(balance));
    }

    public List<ClassIssueSummary> classSummaries(List<BookIssue> issues) {
        Map<String, List<BookIssue>> grouped = new LinkedHashMap<>();
        issues.forEach(issue -> grouped
                .computeIfAbsent(issue.getStudent().getGrade(), ignored -> new java.util.ArrayList<>())
                .add(issue));
        return grouped.entrySet().stream()
                .map(entry -> {
                    IssueTotals totals = totals(entry.getValue());
                    return new ClassIssueSummary(entry.getKey(), totals.count(), totals.totalDue(), totals.collected(), totals.balance());
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public List<BookIssue> findBetween(LocalDate startDate, LocalDate endDate) {
        return bookIssueRepository.findByIssueDateBetweenOrderByIssueDateDescIdDesc(startDate, endDate);
    }

    @Transactional(readOnly = true)
    public List<BookIssue> findForStudent(Long studentId) {
        return bookIssueRepository.findByStudentIdOrderByIssueDateDescIdDesc(studentId);
    }

    @Transactional(readOnly = true)
    public BookIssue getDetailed(Long id) {
        return bookIssueRepository.findDetailedById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book issue not found"));
    }

    @Transactional
    public BookIssue create(IssueForm form) {
        Student student = studentRepository.findById(form.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
        if (!student.getGrade().equals(form.getSelectedClass())) {
            throw new BusinessRuleException("Selected student does not belong to the selected class");
        }

        BigDecimal subtotal = money(form.getUnitPrice()).multiply(BigDecimal.valueOf(form.getQuantity()));
        BigDecimal discount = money(form.getDiscount());
        if (discount.compareTo(subtotal) > 0) {
            throw new BusinessRuleException("Discount cannot be greater than subtotal");
        }

        BookIssue issue = new BookIssue();
        issue.setStudent(student);
        issue.setIssueDate(form.getIssueDate());
        issue.setBookTitle(trim(form.getBookTitle()));
        issue.setQuantity(form.getQuantity());
        issue.setUnitPrice(money(form.getUnitPrice()));
        issue.setDiscount(discount);
        issue.setTotalDue(subtotal.subtract(discount).setScale(2, RoundingMode.HALF_UP));
        issue.setRemarks(trim(form.getRemarks()));
        issue.setStatus(issue.getTotalDue().compareTo(BigDecimal.ZERO) == 0 ? IssueStatus.PAID : IssueStatus.PENDING);
        return bookIssueRepository.save(issue);
    }

    @Transactional
    public void refreshStatus(BookIssue issue) {
        BigDecimal collected = issue.getCollectedAmount();
        if (collected.compareTo(BigDecimal.ZERO) == 0 && issue.getTotalDue().compareTo(BigDecimal.ZERO) > 0) {
            issue.setStatus(IssueStatus.PENDING);
        } else if (collected.compareTo(issue.getTotalDue()) >= 0) {
            issue.setStatus(IssueStatus.PAID);
        } else {
            issue.setStatus(IssueStatus.PARTIAL);
        }
        bookIssueRepository.save(issue);
    }

    @Transactional
    public void deleteIfNoPayments(Long id) {
        BookIssue issue = getDetailed(id);
        if (!issue.getPayments().isEmpty()) {
            throw new BusinessRuleException("Cannot delete a book issue after payment is recorded");
        }
        bookIssueRepository.delete(issue);
    }

    public boolean matchesSearch(BookIssue issue, String search) {
        if (!StringUtils.hasText(search)) {
            return true;
        }
        String term = search.trim().toLowerCase();
        return issue.getBookTitle().toLowerCase().contains(term)
                || issue.getStudent().getName().toLowerCase().contains(term)
                || issue.getStudent().getGrade().toLowerCase().contains(term);
    }

    private BigDecimal money(BigDecimal value) {
        return (value == null ? BigDecimal.ZERO : value).setScale(2, RoundingMode.HALF_UP);
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }

    public record IssueTotals(int count, BigDecimal totalDue, BigDecimal collected, BigDecimal balance) {
    }

    public record ClassIssueSummary(String className, int count, BigDecimal totalDue, BigDecimal collected, BigDecimal balance) {
    }
}

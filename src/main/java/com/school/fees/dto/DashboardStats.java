package com.school.fees.dto;

import java.math.BigDecimal;

public record DashboardStats(
        BigDecimal todayCollection,
        BigDecimal totalCollection,
        BigDecimal pendingAmount,
        long pendingCount,
        long partialCount,
        long paidCount
) {
}

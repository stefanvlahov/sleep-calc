package org.svlahov.sleepcalc.dto;

import java.util.List;

public record WeeklyReportDTO(
        double netSleepDebt,
        double netSleepSurplus,
        double percentageChange, // vs previous week
        List<DailyReportItem> dailyItems) {
}

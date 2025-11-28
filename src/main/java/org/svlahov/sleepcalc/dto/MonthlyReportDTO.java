package org.svlahov.sleepcalc.dto;

import java.util.List;

public record MonthlyReportDTO(
        double averageHoursSlept,
        double percentageChange, // vs previous 4 weeks
        List<WeeklyReportItem> weeklyItems) {
}

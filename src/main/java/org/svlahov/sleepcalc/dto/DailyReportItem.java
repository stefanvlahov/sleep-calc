package org.svlahov.sleepcalc.dto;

import java.time.LocalDate;

public record DailyReportItem(
        LocalDate date,
        double hoursSlept,
        double debtChange,
        double surplusChange) {
}

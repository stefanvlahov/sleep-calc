package org.svlahov.sleepcalc.service;

import org.svlahov.sleepcalc.dto.MonthlyReportDTO;
import org.svlahov.sleepcalc.dto.WeeklyReportDTO;

import java.time.LocalDate;

public interface ReportService {
    WeeklyReportDTO getWeeklyReport(LocalDate date);

    MonthlyReportDTO getMonthlyReport(LocalDate date);

    String exportReport(LocalDate from, LocalDate to);
}

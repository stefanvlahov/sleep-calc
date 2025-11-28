package org.svlahov.sleepcalc.dto;

public record WeeklyReportItem(
        String weekLabel, // e.g., "Week 1"
        double averageHoursSlept) {
}

package org.svlahov.sleepcalc.service;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.util.List;

public interface SleepService {

    record SleepState(double sleepDebt, double sleepSurplus) {}

    record SleepHistoryEntry(
            @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd") LocalDate sleepDate,
            double hoursSlept,
            double sleepDebt,
            double sleepSurplus
    ) {}

    SleepState recordSleep(String timeSlept, LocalDate date);

    SleepState getCurrentSleepState();

    List<SleepHistoryEntry> getSleepHistory();

    List<SleepHistoryEntry> getSleepHistory(LocalDate from, LocalDate to);
}

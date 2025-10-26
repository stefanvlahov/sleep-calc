package org.svlahov.sleepcalc.service;

import java.time.LocalDate;

public interface SleepService {

    record SleepState(double sleepDebt, double sleepSurplus) {}

    SleepState recordSleep(String timeSlept, LocalDate date);

    SleepState getCurrentSleepState();
}

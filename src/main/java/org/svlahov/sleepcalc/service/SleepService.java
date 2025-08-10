package org.svlahov.sleepcalc.service;

public interface SleepService {

    record SleepState(double sleepDebt, double sleepSurplus) {}

    SleepState recordSleep(String userId, double hoursSlept);

    SleepState getCurrentSleepState(String userId);
}

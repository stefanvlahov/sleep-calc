package org.svlahov.sleepcalc.service;

public interface SleepService {

    record SleepState(double sleepDebt, double sleepSurplus) {}

    double recordSleep(String userId, double hoursSlept);

    double getCurrentSleepDebt(String userId);

    void reset(String UserId);
}

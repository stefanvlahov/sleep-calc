package org.svlahov.sleepcalc.service;

public interface SleepService {

    double recordSleep(String userId, double hoursSlept);

    double getCurrentSleepDebt(String userId);

    void reset(String UserId);
}

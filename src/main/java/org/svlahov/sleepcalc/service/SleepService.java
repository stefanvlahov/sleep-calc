package org.svlahov.sleepcalc.service;

public interface SleepService {

    double recordSleep(double hoursSlept);

    double getCurrentSleepDebt();

    void reset();
}

package org.svlahov.sleepcalc.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class SleepServiceTest {

    private SleepService sleepService;

    @BeforeEach
    void setUp() {
        sleepService = new SleepServiceImpl();
    }

    @Test
    @DisplayName("Initial sleep debt should be zero")
    void getInitialSleepDebt_shouldBeZero() {
        assertEquals(0.0, sleepService.getCurrentSleepDebt(), "Initial debt should be 0.0");
    }

    @Test
    @DisplayName("Should increase debt when sleep is less than target")
    void recordSleep_whenLessThanTarget_thenIncreaseDebt() {
        double newDebt = sleepService.recordSleep(6.0);

        assertEquals(1.5, newDebt, 0.01);
        assertEquals(1.5, sleepService.getCurrentSleepDebt(), 0.01);
    }

    @Test
    @DisplayName("Should decrease debt when sleep is more than target")
    void recordSleep_whenMoreThanTarget_thenDecreaseDebt() {
        double newDebt = sleepService.recordSleep(8.5);

        assertEquals(-1.0, newDebt, 0.01);
    }

    @Test
    @DisplayName("Should not change debt when sleep is exactly the target")
    void recordSleep_whenExactlyTarget_thenNoChange() {
        double newDebt = sleepService.recordSleep(7.5);

        assertEquals(0.0, newDebt, 0.01);
    }

    @Test
    @DisplayName("Should apply diminished recovery when debt is high")
    void recordSleep_whenDebtIsHigh_thenApplyDiminishedRecovery() {
        sleepService.recordSleep(2.5);

        double newDebt = sleepService.recordSleep(9.5);

        assertEquals(3.35, newDebt, 0.01, "Diminished recovery was not applied correctly.");
    }

    @Test
    @DisplayName("Should throw exception for negative sleep hours")
    void recordSleep_whenInputIsNegative_thenThrowsException() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> sleepService.recordSleep(-1.0)
        );

        assertEquals("Hours slept cannot be negative.", exception.getMessage());
    }
}

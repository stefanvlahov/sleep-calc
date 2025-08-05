package org.svlahov.sleepcalc.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.atomic.AtomicReference;

public class SleepServiceImpl implements SleepService {

    private final AtomicReference<BigDecimal> currentSleepDebt = new AtomicReference<>(BigDecimal.ZERO);
    private static final BigDecimal TARGET_SLEEP = new BigDecimal("7.5");
    private static final BigDecimal ZERO = BigDecimal.ZERO;
    private static final BigDecimal ONE = BigDecimal.ONE;

    private static final BigDecimal MAX_EFFECTIVE_DEBT = new BigDecimal("20.0");
    private static final BigDecimal MIN_RECOVERY_FACTOR = new BigDecimal("0.3");

    @Override
    public double getCurrentSleepDebt() {
        return currentSleepDebt.get().setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    @Override
    public double recordSleep(double hoursSleptValue) {
        BigDecimal hoursSlept = BigDecimal.valueOf(hoursSleptValue);
        if (hoursSlept.compareTo(ZERO) < 0) {
            throw new IllegalArgumentException("Hours slept cannot be negative.");
        }

        BigDecimal newDebt = currentSleepDebt.updateAndGet(previousDebt -> {
            BigDecimal difference = hoursSlept.subtract(TARGET_SLEEP);
            BigDecimal debtChange;

            if (difference.compareTo(ZERO) > 0) { // Slept MORE than target
                BigDecimal recoveryFactor;
                if (previousDebt.compareTo(ZERO) <= 0) {
                    recoveryFactor = ONE;
                } else {
                    BigDecimal debtRatio = previousDebt.divide(MAX_EFFECTIVE_DEBT, 4, RoundingMode.HALF_UP);
                    BigDecimal factorReduction = debtRatio.multiply(ONE.subtract(MIN_RECOVERY_FACTOR));
                    BigDecimal calculatedFactor = ONE.subtract(factorReduction);
                    recoveryFactor = calculatedFactor.max(MIN_RECOVERY_FACTOR);
                }
                debtChange = difference.multiply(recoveryFactor).negate();
            } else { // Slept LESS than or EQUAL to target
                debtChange = difference.negate();
            }
            return previousDebt.add(debtChange);
        });

        return newDebt.setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    @Override
    public void reset() {
        this.currentSleepDebt.set(BigDecimal.ZERO);
    }
}

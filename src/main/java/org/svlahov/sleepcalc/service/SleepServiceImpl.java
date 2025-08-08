package org.svlahov.sleepcalc.service;

import org.springframework.stereotype.Service;
import org.svlahov.sleepcalc.entity.SleepData;
import org.svlahov.sleepcalc.repository.SleepDataRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class SleepServiceImpl implements SleepService {

    // Sleep calculation constants
    private static final BigDecimal TARGET_SLEEP_HOURS = new BigDecimal("7.5");
    private static final BigDecimal MAX_EFFECTIVE_DEBT = new BigDecimal("20.0");
    private static final BigDecimal MIN_RECOVERY_FACTOR = new BigDecimal("0.3");

    // Common BigDecimal values
    private static final BigDecimal ZERO = BigDecimal.ZERO;
    private static final BigDecimal ONE = BigDecimal.ONE;

    // Current state
    private final SleepDataRepository sleepDataRepository;

    public SleepServiceImpl(SleepDataRepository sleepDataRepository) {
        this.sleepDataRepository = sleepDataRepository;
    }

    @Override
    public double getCurrentSleepDebt(String userId) {
        return sleepDataRepository.findByUserId(userId)
                                    .map(data -> formatDebtValue(data.getSleepDebt()))
                                    .orElse(0.0);
    }

    @Override
    public double recordSleep(String userId, double hoursSleptValue) {
        SleepData data = sleepDataRepository.findByUserId(userId).orElseGet(() -> new SleepData(userId));

        BigDecimal hoursSlept = BigDecimal.valueOf(hoursSleptValue);
        validateHoursSlept(hoursSlept);

        BigDecimal newDebt = calculateNewDebt(data.getSleepDebt(), hoursSlept);
        data.setSleepDebt(newDebt);

        sleepDataRepository.save(data);

        return formatDebtValue(data.getSleepDebt());
    }

    @Override
    public void reset(String userId) {
        sleepDataRepository.findByUserId(userId).ifPresent(sleepDataRepository::delete);
    }

    // Helper methods for improved readability

    private void validateHoursSlept(BigDecimal hoursSlept) {
        if (hoursSlept.compareTo(ZERO) < 0) {
            throw new IllegalArgumentException("Hours slept cannot be negative.");
        }
    }

    private BigDecimal calculateNewDebt(BigDecimal previousDebt, BigDecimal hoursSlept) {
        BigDecimal sleepDifference = hoursSlept.subtract(TARGET_SLEEP_HOURS);
        BigDecimal debtChange;

        if (sleepDifference.compareTo(ZERO) > 0) {
            // Slept more than target - reduce debt based on recovery factor
            BigDecimal recoveryFactor = calculateRecoveryFactor(previousDebt);
            debtChange = sleepDifference.multiply(recoveryFactor).negate();
        } else {
            // Slept less than or equal to target - increase debt by full difference
            debtChange = sleepDifference.negate();
        }

        return previousDebt.add(debtChange);
    }

    private BigDecimal calculateRecoveryFactor(BigDecimal currentDebt) {
        // If no debt or negative debt (sleep surplus), recovery is 100% effective
        if (currentDebt.compareTo(ZERO) <= 0) {
            return ONE;
        }

        // As debt increases, recovery becomes less effective
        // Recovery factor decreases linearly from 1.0 to MIN_RECOVERY_FACTOR (0.3)
        BigDecimal debtRatio = currentDebt.divide(MAX_EFFECTIVE_DEBT, 4, RoundingMode.HALF_UP);
        BigDecimal factorReduction = debtRatio.multiply(ONE.subtract(MIN_RECOVERY_FACTOR));
        BigDecimal calculatedFactor = ONE.subtract(factorReduction);

        // Ensure recovery factor doesn't go below minimum
        return calculatedFactor.max(MIN_RECOVERY_FACTOR);
    }

    private double formatDebtValue(BigDecimal debt) {
        return debt.setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}

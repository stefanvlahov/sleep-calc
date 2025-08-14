package org.svlahov.sleepcalc.service;

import org.springframework.stereotype.Service;
import org.svlahov.sleepcalc.entity.SleepData;
import org.svlahov.sleepcalc.repository.SleepDataRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;

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
    public SleepState getCurrentSleepState(String userId) {
        return sleepDataRepository.findByUserId(userId)
                .map(data -> new SleepState(formatDebtValue(data.getSleepDebt()), formatDebtValue(data.getSleepSurplus())))
                .orElse(new SleepState(0.0, 0.0));
    }

    @Override
    public SleepState recordSleep(String userId, String timeSlept) {
        SleepData data = sleepDataRepository.findByUserId(userId).orElseGet(() -> new SleepData(userId));

        BigDecimal hoursSleptDecimal = parseTimeSleptToDecimal(timeSlept);

        BigDecimal sleepDiffernece = hoursSleptDecimal.subtract(TARGET_SLEEP_HOURS);

        if (sleepDiffernece.compareTo(ZERO) > 0) {
            applyExtraSleep(data, sleepDiffernece);
        } else if (sleepDiffernece.compareTo(ZERO) < 0) {
            applySleepShortfall(data, sleepDiffernece.negate());
        }

        SleepData savedData = sleepDataRepository.save(data);
        return new SleepState(formatDebtValue(savedData.getSleepDebt()), formatDebtValue(savedData.getSleepSurplus()));
    }

    // Helper methods for improved readability

    private BigDecimal parseTimeSleptToDecimal(String timeSlept) {
        if (timeSlept == null || timeSlept.isBlank()) {
            throw new IllegalArgumentException("Time slept cannot be empty.");
        }

        if (timeSlept.contains(":")) {
            String[] parts = timeSlept.split(":");
            if (parts.length != 2) {
                throw new IllegalArgumentException("Invalid time format. Please use HH:mm");
            }
            try {
                int hours = Integer.parseInt(parts[0]);
                int minutes = Integer.parseInt(parts[1]);

                if (hours < 0 || hours >= 24) {
                    throw new IllegalArgumentException("Hours must be between 0 and 23.");
                }
                if (minutes < 0 || minutes >= 60) {
                    throw new IllegalArgumentException("Minutes must be between 0 and 59.");
                }

                BigDecimal minuitedecimal = new BigDecimal(minutes).divide(new BigDecimal(60), 2, RoundingMode.HALF_UP);
                return new BigDecimal(hours).add(minuitedecimal);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid time format. Hours and minutes must be numbers.");
            }
        }

        try {
            BigDecimal hours = new BigDecimal(timeSlept);
            if (hours.compareTo(ZERO) < 0) {
                throw new IllegalArgumentException("Hours slept cannot be negative.");
            }
            return hours;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid number format..");
        }
    }

    private void applyExtraSleep(SleepData data, BigDecimal extraSleep) {
        BigDecimal currentDebt = data.getSleepDebt();

        if (currentDebt.compareTo(ZERO) <= 0) {
            data.setSleepSurplus(data.getSleepSurplus().add(extraSleep));
            return;
        }

        BigDecimal recoveryFactor = calculateRecoveryFactor(currentDebt);
        BigDecimal debtReductionAmount = extraSleep.multiply(recoveryFactor);

        BigDecimal actualDebtPaid = debtReductionAmount.min(currentDebt);

        BigDecimal surplusToAdd = extraSleep.subtract(actualDebtPaid);

        data.setSleepDebt(currentDebt.subtract(actualDebtPaid));
        if (surplusToAdd.compareTo(ZERO) > 0) {
            data.setSleepSurplus(data.getSleepSurplus().add(surplusToAdd));
        }
    }

    private void applySleepShortfall(SleepData data, BigDecimal shortfall) {
        BigDecimal currentSurplus = data.getSleepSurplus();
        BigDecimal surplusToUse = shortfall.min(currentSurplus);
        data.setSleepSurplus(currentSurplus.subtract(surplusToUse));

        BigDecimal remainingShortfall = shortfall.subtract(surplusToUse);
        data.setSleepDebt(data.getSleepDebt().add(remainingShortfall));
    }

    private BigDecimal calculateRecoveryFactor(BigDecimal currentDebt) {
        // If no debt or negative debt (sleep surplus), recovery is 100% effective
        if (currentDebt.compareTo(ONE) <= 0) {
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

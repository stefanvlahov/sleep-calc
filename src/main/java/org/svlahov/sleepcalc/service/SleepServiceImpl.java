package org.svlahov.sleepcalc.service;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.svlahov.sleepcalc.entity.SleepData;
import org.svlahov.sleepcalc.entity.User;
import org.svlahov.sleepcalc.repository.SleepDataRepository;
import org.svlahov.sleepcalc.repository.UserRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalTime;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Optional;

@Service
public class SleepServiceImpl implements SleepService {

    // Sleep calculation constants
    private static final BigDecimal TARGET_SLEEP_HOURS = new BigDecimal("7.5");
    private static final BigDecimal MAX_EFFECTIVE_DEBT = new BigDecimal("20.0");
    private static final BigDecimal MIN_RECOVERY_FACTOR = new BigDecimal("0.3");
    private static final int MINUTES_PER_HOUR = 60;
    private static final int DECIMAL_SCALE = 2;

    // Common BigDecimal values
    private static final BigDecimal ZERO = BigDecimal.ZERO;
    private static final BigDecimal ONE = BigDecimal.ONE;

    private record CumulativeState(BigDecimal sleepDebt, BigDecimal sleepSurplus) {}

    // Current state
    private final SleepDataRepository sleepDataRepository;
    private final UserRepository userRepository;

    public SleepServiceImpl(SleepDataRepository sleepDataRepository, UserRepository userRepository) {
        this.sleepDataRepository = sleepDataRepository;
        this.userRepository = userRepository;
    }

    @Override
    public SleepState getCurrentSleepState() {
        User currentUser = getCurrentUser();
        return sleepDataRepository.findTopByUser_UsernameOrderBySleepDateDesc(currentUser.getUsername())
                .map(data -> new SleepState(formatDebtValue(data.getSleepDebt()), formatDebtValue(data.getSleepSurplus())))
                .orElse(new SleepState(0.0, 0.0));
    }

    @Override
    public SleepState recordSleep(String timeSlept, LocalDate date) {
        User currentUser = getCurrentUser();
        BigDecimal hoursSleptDecimal = parseTimeSleptToDecimal(timeSlept);

        Optional<SleepData> latestEntry = sleepDataRepository.findTopByUser_UsernameOrderBySleepDateDesc(currentUser.getUsername());
        CumulativeState previousState = latestEntry
                .map(data -> new CumulativeState(data.getSleepDebt(), data.getSleepSurplus()))
                .orElse(new CumulativeState(ZERO, ZERO));

        BigDecimal sleepDifference = hoursSleptDecimal.subtract(TARGET_SLEEP_HOURS);
        CumulativeState newState;

        if (sleepDifference.compareTo(ZERO) > 0) {
            newState = applyExtraSleep(previousState, sleepDifference);
        } else if (sleepDifference.compareTo(ZERO) < 0) {
            newState = applySleepShortfall(previousState, sleepDifference.negate());
        } else {
            newState = previousState;
        }

        SleepData newSleepEntry = new SleepData(currentUser, date, hoursSleptDecimal, newState.sleepDebt(), newState.sleepSurplus());

        SleepData savedData = sleepDataRepository.save(newSleepEntry);
        return new SleepState(formatDebtValue(savedData.getSleepDebt()), formatDebtValue(savedData.getSleepSurplus()));
    }

    // Helper methods for improved readability

    private User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username;
        if (principal instanceof UserDetails) {
            username = ((UserDetails) principal).getUsername();
        } else {
            username = principal.toString();
        }

        return userRepository.findByUsername(username).orElseThrow(() -> new IllegalStateException("Authenticated user not found in database"));

    }

    private BigDecimal parseTimeSleptToDecimal(String timeSlept) {
        if (timeSlept == null || timeSlept.isBlank()) {
            throw new IllegalArgumentException("Time slept cannot be empty.");
        }

        if (timeSlept.contains(":")) {
            return parseTimeUsingLocalTime(timeSlept);
        }

        return parseDecimalFormat(timeSlept);
    }

    private BigDecimal parseTimeUsingLocalTime(String timeSlept) {
        try {
            if (timeSlept.startsWith("24:")) {
                throw new DateTimeParseException("Hour 24 is not a valid input.", timeSlept, 0);
            }
            LocalTime time = LocalTime.parse(timeSlept, DateTimeFormatter.ofPattern("H:mm"));
            int hours = time.getHour();
            int minutes = time.getMinute();

            BigDecimal minuteDecimal = new BigDecimal(minutes)
                    .divide(new BigDecimal(MINUTES_PER_HOUR), DECIMAL_SCALE, RoundingMode.HALF_UP);
            return new BigDecimal(hours).add(minuteDecimal);

        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid time format. Please use HH:mm", e);
        }
    }

    private BigDecimal parseDecimalFormat(String timeSlept) {
        try {
            BigDecimal hours = new BigDecimal(timeSlept);
            if (hours.compareTo(ZERO) < 0) {
                throw new IllegalArgumentException("Hours slept cannot be negative.");
            }
            return hours;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid number format.");
        }
    }

    private CumulativeState applyExtraSleep(CumulativeState previousState, BigDecimal extraSleep) {
        BigDecimal currentDebt = previousState.sleepDebt();
        BigDecimal currentSurplus = previousState.sleepSurplus();

        if (currentDebt.compareTo(ZERO) <= 0) {
            return new CumulativeState(currentDebt, currentSurplus.add(extraSleep));
        }

        BigDecimal recoveryFactor = calculateRecoveryFactor(currentDebt);
        BigDecimal debtReductionAmount = extraSleep.multiply(recoveryFactor);

        BigDecimal actualDebtPaid = debtReductionAmount.min(currentDebt);
        BigDecimal newDebt = currentDebt.subtract(actualDebtPaid);

        BigDecimal sleepPowerUsed = (recoveryFactor.compareTo(ZERO) > 0) ? actualDebtPaid.divide(recoveryFactor, 4, RoundingMode.HALF_UP) : ZERO;
        BigDecimal surplusToAdd = extraSleep.subtract(sleepPowerUsed).max(ZERO);

        BigDecimal newSurplus = currentSurplus.add(surplusToAdd);

        return new CumulativeState(newDebt, newSurplus);
    }

    private CumulativeState applySleepShortfall(CumulativeState previousState, BigDecimal shortfall) {
        BigDecimal currentDebt = previousState.sleepDebt();
        BigDecimal currentSurplus = previousState.sleepSurplus();

        BigDecimal surplusToUse = shortfall.min(currentSurplus);
        BigDecimal newSurplus = currentSurplus.subtract(surplusToUse);

        BigDecimal remainingShortfall = shortfall.subtract(surplusToUse);
        BigDecimal newDebt = currentDebt.add(remainingShortfall);

        return new CumulativeState(newDebt, newSurplus);
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

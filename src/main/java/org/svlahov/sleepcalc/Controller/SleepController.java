package org.svlahov.sleepcalc.Controller;

import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.atomic.AtomicReference;

@RestController
@RequestMapping("api/sleep")
@CrossOrigin(origins = "http://localhost:3000")
public class SleepController {

    private AtomicReference<BigDecimal> currentSleepDebt = new AtomicReference<>(BigDecimal.ZERO);
    private static final BigDecimal TARGET_SLEEP = new BigDecimal("7.5");
    private static final BigDecimal ZERO = BigDecimal.ZERO;
    private static final BigDecimal ONE = BigDecimal.ONE;

    private static final BigDecimal MAX_EFFECTIVE_DEBT = new BigDecimal("20.0");
    private static final BigDecimal MIN_RECOVERY_FACTOR = new BigDecimal("0.3");

    @GetMapping("/debt")
    public double getCurrentSleepDebt() {
        return currentSleepDebt.get().setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    @PostMapping
    public double recordSleep(@RequestBody SleepInput input) {
        BigDecimal hoursSlept = BigDecimal.valueOf(input.getHoursSlept());
        if (hoursSlept.compareTo(ZERO) < 0) {
            throw new IllegalArgumentException("Hours slept cannot be negative");
        }

        BigDecimal newDebt = currentSleepDebt.updateAndGet(previousDebt -> {
            BigDecimal difference = hoursSlept.subtract(TARGET_SLEEP);
            BigDecimal debtChange;

            if (difference.compareTo(ZERO) > 0) {
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

                System.out.println("Sleep > Target. Debt: " + previousDebt + ", Diff: " + difference + ", Factor: " + recoveryFactor + ", Change: " + debtChange);

            } else {
                debtChange = difference.negate();
                System.out.println("Sleep <= Target. Debt: " + previousDebt + ", Diff: " + difference + ", Change: " + debtChange);
            }

            return previousDebt.add(debtChange);
        });

        System.out.println("New Debt: " + newDebt.setScale(2, RoundingMode.HALF_UP));

        return newDebt.setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    public static class SleepInput {
        private double hoursSlept;

        public double getHoursSlept() {
            return hoursSlept;
        }

        public void setHoursSlept(double hoursSlept) {
            this.hoursSlept = hoursSlept;
        }
    }

}
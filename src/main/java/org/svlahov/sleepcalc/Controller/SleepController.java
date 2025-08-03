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

    @GetMapping("/debt")
    public double getCurrentSleepDebt() {
        return currentSleepDebt.get().setScale(2, RoundingMode.HALF_UP).doubleValue();
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
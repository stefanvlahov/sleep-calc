package org.svlahov.sleepcalc.controller;

import org.springframework.web.bind.annotation.*;
import org.svlahov.sleepcalc.service.SleepService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.atomic.AtomicReference;

@RestController
@RequestMapping("api/sleep")
@CrossOrigin(origins = "http://localhost:3000")
public class SleepController {

    private final SleepService sleepService;

    public SleepController(SleepService sleepService) {
        this.sleepService = sleepService;
    }

    @GetMapping("/debt")
    public double getCurrentSleepDebt() {
        return sleepService.getCurrentSleepDebt();
    }

    @PostMapping
    public double recordSleep(@RequestBody SleepInput sleepInput) {
        return sleepService.recordSleep(sleepInput.getHoursSlept());
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

    void reset() {
        sleepService.reset();
    }

}
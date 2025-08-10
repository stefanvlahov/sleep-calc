package org.svlahov.sleepcalc.controller;

import org.springframework.web.bind.annotation.*;
import org.svlahov.sleepcalc.service.SleepService;
import org.svlahov.sleepcalc.service.SleepService.SleepState;

@RestController
@RequestMapping("api/sleep")
@CrossOrigin(origins = "http://localhost:3000")
public class SleepController {

    private final SleepService sleepService;

    public SleepController(SleepService sleepService) {
        this.sleepService = sleepService;
    }

    @GetMapping("/state")
    public SleepState getCurrentSleepState() {
        return sleepService.getCurrentSleepState("default-user");
    }

    @PostMapping
    public SleepState recordSleep(@RequestBody SleepInput sleepInput) {
        return sleepService.recordSleep("default-user", sleepInput.getHoursSlept());
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
package org.svlahov.sleepcalc.controller;

import org.springframework.web.bind.annotation.*;
import org.svlahov.sleepcalc.service.SleepService;
import org.svlahov.sleepcalc.service.SleepService.SleepState;

@RestController
@RequestMapping("api/sleep")
@CrossOrigin(origins = "http://localhost:5173")
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
        return sleepService.recordSleep("default-user", sleepInput.getTimeSlept());
    }

    public static class SleepInput {
        private String timeSlept;

        public String getTimeSlept() {
            return timeSlept;
        }

        public void setTimeSlept(String timeSlept) {
            this.timeSlept = timeSlept;
        }
    }
}
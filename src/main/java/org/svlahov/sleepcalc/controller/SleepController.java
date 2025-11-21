package org.svlahov.sleepcalc.controller;

import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.svlahov.sleepcalc.service.SleepService;
import org.svlahov.sleepcalc.service.SleepService.SleepState;
import org.svlahov.sleepcalc.service.SleepService.SleepHistoryEntry;

@RestController
@RequestMapping("api/sleep")
public class SleepController {

    private final SleepService sleepService;

    public SleepController(SleepService sleepService) {
        this.sleepService = sleepService;
    }

    @GetMapping("/state")
    public SleepState getCurrentSleepState() {
        return sleepService.getCurrentSleepState();
    }

    @GetMapping("/history")
    public List<SleepHistoryEntry> getSleepHistory() {
        return sleepService.getSleepHistory();
    }

    @GetMapping("/history/range")
    public List<SleepHistoryEntry> getSleepHistoryRange(
            @RequestParam("from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam("to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return sleepService.getSleepHistory(from, to);
    }

    @PostMapping
    public SleepState recordSleep(@RequestBody SleepInput sleepInput) {
        return sleepService.recordSleep(sleepInput.getTimeSlept(), sleepInput.getDate());
    }

    public static class SleepInput {
        private String timeSlept;
        private LocalDate date;

        public String getTimeSlept() {
            return timeSlept;
        }

        public void setTimeSlept(String timeSlept) {
            this.timeSlept = timeSlept;
        }

        public LocalDate getDate() {
            return date;
        }

        public void setDate(LocalDate date) {
            this.date = date;
        }
    }
}
package org.svlahov.sleepcalc.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/sleep")
public class SleepController {

    private static final double IDEAL_SLEEP_HOURS = 7.5;

    @PostMapping
    public ResponseEntity<Map<String, Object>> addSleepEntry(@RequestBody SleepEntry sleepEntry) {
        double hoursSlept = sleepEntry.getHoursSlept();
        double sleepDebtChange = calculateSleepDebtChange(hoursSlept);
        
        Map<String, Object> response = new HashMap<>();
        response.put("sleepDebtChange", sleepDebtChange);
        
        String message;
        if (sleepDebtChange > 0) {
            message = "Sleep debt increased by " + sleepDebtChange + " hours";
        } else {
            message = "Sleep debt decreased by " + Math.abs(sleepDebtChange) + " hours";
        }
        
        response.put("message", message);
        
        return ResponseEntity.ok(response);
    }
    
    private double calculateSleepDebtChange(double hoursSlept) {
        return IDEAL_SLEEP_HOURS - hoursSlept;
    }
    
    // Inner class to represent the request body
    public static class SleepEntry {
        private double hoursSlept;
        
        public double getHoursSlept() {
            return hoursSlept;
        }
        
        public void setHoursSlept(double hoursSlept) {
            this.hoursSlept = hoursSlept;
        }
    }
}
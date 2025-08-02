package org.svlahov.sleepcalc.Controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.RoundingMode;

@RestController
@RequestMapping("api/sleep")
@CrossOrigin(origins = "http://localhost:3000")
public class SleepController {

    @GetMapping("/debt")
    public double getCurrentSleepDebt() {
        return 0.0;
    }

}
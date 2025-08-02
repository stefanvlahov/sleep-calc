package org.svlahov.sleepcalc.Controller;

import jdk.internal.net.http.HttpClientImpl;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.atomic.AtomicReference;

@RestController
@RequestMapping("api/sleep")
@CrossOrigin(origins = "http://localhost:3000")
public class SleepController {

    private AtomicReference<BigDecimal> currentSleepDebt = new AtomicReference<>(BigDecimal.ZERO);

    @GetMapping("/debt")
    public double getCurrentSleepDebt() {
        return currentSleepDebt.get().setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

}
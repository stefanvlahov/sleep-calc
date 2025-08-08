package org.svlahov.sleepcalc.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

import java.math.BigDecimal;

@Entity
public class SleepData {

    @Id
    @GeneratedValue
    private Long id;

    @Column(unique = true, nullable = false)
    private String userId;

    private BigDecimal sleepDebt = BigDecimal.ZERO;
    private BigDecimal sleepSurplus = BigDecimal.ZERO;

    protected SleepData() {}

    public SleepData(String userId) {
        this.userId = userId;
    }

    // --- Getters and Setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public BigDecimal getSleepDebt() { return sleepDebt; }
    public void setSleepDebt(BigDecimal sleepDebt) { this.sleepDebt = sleepDebt; }
    public BigDecimal getSleepSurplus() { return sleepSurplus; }
    public void setSleepSurplus(BigDecimal sleepSurplus) { this.sleepSurplus = sleepSurplus; }
}

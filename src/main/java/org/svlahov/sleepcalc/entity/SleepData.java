package org.svlahov.sleepcalc.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
public class SleepData {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private BigDecimal sleepDebt = BigDecimal.ZERO;
    private BigDecimal sleepSurplus = BigDecimal.ZERO;

    protected SleepData() {}

    public SleepData(User user) {
        this.user = user;
    }

    // --- Getters and Setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public BigDecimal getSleepDebt() { return sleepDebt; }
    public void setSleepDebt(BigDecimal sleepDebt) { this.sleepDebt = sleepDebt; }
    public BigDecimal getSleepSurplus() { return sleepSurplus; }
    public void setSleepSurplus(BigDecimal sleepSurplus) { this.sleepSurplus = sleepSurplus; }
}

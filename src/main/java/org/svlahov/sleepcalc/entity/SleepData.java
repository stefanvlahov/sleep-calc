package org.svlahov.sleepcalc.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.math.BigDecimal;

@Entity
public class SleepData {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private BigDecimal hoursSlept;

    @Column(nullable = false)
    private BigDecimal sleepDebt = BigDecimal.ZERO;

    @Column(nullable = false)
    private BigDecimal sleepSurplus = BigDecimal.ZERO;

    @Column(nullable = false)
    private LocalDate sleepDate;

    protected SleepData() {
    }

    public SleepData(User user, LocalDate sleepDate, BigDecimal hoursSlept, BigDecimal sleepDebt, BigDecimal sleepSurplus) {
        this.user = user;
        this.sleepDate = sleepDate;
        this.hoursSlept = hoursSlept;
        this.sleepDebt = sleepDebt;
        this.sleepSurplus = sleepSurplus;
    }

    // --- Getters and Setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public BigDecimal getHoursSlept() { return hoursSlept; }

    public void setHoursSlept(BigDecimal hoursSlept) { this.hoursSlept = hoursSlept; }

    public BigDecimal getSleepDebt() {
        return sleepDebt;
    }

    public void setSleepDebt(BigDecimal sleepDebt) {
        this.sleepDebt = sleepDebt;
    }

    public BigDecimal getSleepSurplus() {
        return sleepSurplus;
    }

    public void setSleepSurplus(BigDecimal sleepSurplus) {
        this.sleepSurplus = sleepSurplus;
    }

    public LocalDate getSleepDate() {
        return sleepDate;
    }

    public void setSleepDate(LocalDate sleepDate) {
        this.sleepDate = sleepDate;
    }
}

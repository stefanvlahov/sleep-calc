package org.svlahov.sleepcalc.service;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.svlahov.sleepcalc.dto.DailyReportItem;
import org.svlahov.sleepcalc.dto.MonthlyReportDTO;
import org.svlahov.sleepcalc.dto.WeeklyReportDTO;
import org.svlahov.sleepcalc.dto.WeeklyReportItem;
import org.svlahov.sleepcalc.entity.SleepData;
import org.svlahov.sleepcalc.entity.User;
import org.svlahov.sleepcalc.repository.SleepDataRepository;
import org.svlahov.sleepcalc.repository.UserRepository;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReportServiceImpl implements ReportService {

    private final SleepDataRepository sleepDataRepository;
    private final UserRepository userRepository;

    public ReportServiceImpl(SleepDataRepository sleepDataRepository, UserRepository userRepository) {
        this.sleepDataRepository = sleepDataRepository;
        this.userRepository = userRepository;
    }

    @Override
    public WeeklyReportDTO getWeeklyReport(LocalDate date) {
        User currentUser = getCurrentUser();
        LocalDate startOfWeek = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate endOfWeek = date.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

        List<SleepData> currentWeekData = sleepDataRepository.findByUser_UsernameAndSleepDateBetween(
                currentUser.getUsername(), startOfWeek, endOfWeek);

        // Calculate previous week for trend
        LocalDate startOfPrevWeek = startOfWeek.minusWeeks(1);
        LocalDate endOfPrevWeek = endOfWeek.minusWeeks(1);
        List<SleepData> prevWeekData = sleepDataRepository.findByUser_UsernameAndSleepDateBetween(
                currentUser.getUsername(), startOfPrevWeek, endOfPrevWeek);

        double currentWeekNet = calculateNetSleepScore(currentWeekData);
        double prevWeekNet = calculateNetSleepScore(prevWeekData);

        double percentageChange = calculatePercentageChange(currentWeekNet, prevWeekNet);

        // Build daily items
        Map<LocalDate, SleepData> dataMap = currentWeekData.stream()
                .collect(Collectors.toMap(SleepData::getSleepDate, data -> data));

        List<DailyReportItem> dailyItems = new ArrayList<>();
        LocalDate current = startOfWeek;
        while (!current.isAfter(endOfWeek)) {
            SleepData data = dataMap.get(current);
            if (data != null) {
                // Calculate daily change (this is a simplification, ideally we'd track daily
                // delta)
                // For now, we'll just show the absolute values or 0 if missing
                // Note: The UI shows a bar chart, likely just hours slept or net score.
                // The DTO has debtChange/surplusChange. Let's use the raw values for now.
                // Actually, to show "Weekly Sleep Debt/Surplus", we need the cumulative change?
                // Or just the sum of hours vs target?
                // The image shows "+5h 30m" which implies a sum of deltas.
                // Let's assume the daily items are for the chart.
                dailyItems.add(new DailyReportItem(
                        current,
                        data.getHoursSlept().doubleValue(),
                        0, // Placeholder, logic for daily delta is complex without history
                        0 // Placeholder
                ));
            } else {
                dailyItems.add(new DailyReportItem(current, 0, 0, 0));
            }
            current = current.plusDays(1);
        }

        // Calculate net debt/surplus for the week
        // This is the sum of (HoursSlept - Target) for each day
        // Target is 7.5
        double targetHours = 7.5;
        double netSurplus = currentWeekData.stream()
                .mapToDouble(d -> d.getHoursSlept().doubleValue() - targetHours)
                .sum();

        double netDebt = 0;
        if (netSurplus < 0) {
            netDebt = Math.abs(netSurplus);
            netSurplus = 0;
        }

        return new WeeklyReportDTO(netDebt, netSurplus, percentageChange, dailyItems);
    }

    @Override
    public MonthlyReportDTO getMonthlyReport(LocalDate date) {
        User currentUser = getCurrentUser();
        // "Last 4 Weeks" logic
        // End date is the given date (or end of current week?)
        // Let's assume "Monthly" means the 4 weeks ending on the current week's Sunday.
        LocalDate endOfCurrentWeek = date.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
        LocalDate startOfPeriod = endOfCurrentWeek.minusWeeks(4).plusDays(1); // 4 weeks back

        List<SleepData> periodData = sleepDataRepository.findByUser_UsernameAndSleepDateBetween(
                currentUser.getUsername(), startOfPeriod, endOfCurrentWeek);

        // Previous 4 weeks for trend
        LocalDate endOfPrevPeriod = startOfPeriod.minusDays(1);
        LocalDate startOfPrevPeriod = endOfPrevPeriod.minusWeeks(4).plusDays(1);
        List<SleepData> prevPeriodData = sleepDataRepository.findByUser_UsernameAndSleepDateBetween(
                currentUser.getUsername(), startOfPrevPeriod, endOfPrevPeriod);

        double currentAvg = calculateAverageSleep(periodData);
        double prevAvg = calculateAverageSleep(prevPeriodData);
        double percentageChange = calculatePercentageChange(currentAvg, prevAvg);

        List<WeeklyReportItem> weeklyItems = new ArrayList<>();
        // Group by week
        // We need 4 weeks: Week 1, Week 2, Week 3, Week 4
        // Week 1 is the oldest
        for (int i = 0; i < 4; i++) {
            LocalDate weekStart = startOfPeriod.plusWeeks(i);
            LocalDate weekEnd = weekStart.plusDays(6);

            List<SleepData> weekData = periodData.stream()
                    .filter(d -> !d.getSleepDate().isBefore(weekStart) && !d.getSleepDate().isAfter(weekEnd))
                    .collect(Collectors.toList());

            double weekAvg = calculateAverageSleep(weekData);
            weeklyItems.add(new WeeklyReportItem("Week " + (i + 1), weekAvg));
        }

        return new MonthlyReportDTO(currentAvg, percentageChange, weeklyItems);
    }

    @Override
    public String exportReport(LocalDate from, LocalDate to) {
        User currentUser = getCurrentUser();
        List<SleepData> data = sleepDataRepository.findByUser_UsernameAndSleepDateBetween(
                currentUser.getUsername(), from, to);

        StringBuilder csv = new StringBuilder();
        csv.append("Date,Hours Slept,Sleep Debt,Sleep Surplus\n");
        for (SleepData entry : data) {
            csv.append(entry.getSleepDate()).append(",")
                    .append(entry.getHoursSlept()).append(",")
                    .append(entry.getSleepDebt()).append(",")
                    .append(entry.getSleepSurplus()).append("\n");
        }
        return csv.toString();
    }

    private User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username;
        if (principal instanceof UserDetails) {
            username = ((UserDetails) principal).getUsername();
        } else {
            username = principal.toString();
        }
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found"));
    }

    private double calculateNetSleepScore(List<SleepData> data) {
        // Net score could be sum of (Hours - Target)
        // Or we can use the debt/surplus logic.
        // Let's use sum of hours for simplicity in trend calculation
        return data.stream()
                .mapToDouble(d -> d.getHoursSlept().doubleValue())
                .sum();
    }

    private double calculateAverageSleep(List<SleepData> data) {
        if (data.isEmpty())
            return 0;
        double total = data.stream()
                .mapToDouble(d -> d.getHoursSlept().doubleValue())
                .sum();
        return total / data.size(); // Average per entry? Or per day?
        // Ideally per day, but if days are missing, average of recorded days is safer.
    }

    private double calculatePercentageChange(double current, double previous) {
        if (previous == 0)
            return 0; // Avoid division by zero
        return ((current - previous) / previous) * 100;
    }
}

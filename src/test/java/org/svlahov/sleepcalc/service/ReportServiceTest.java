package org.svlahov.sleepcalc.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.svlahov.sleepcalc.dto.MonthlyReportDTO;
import org.svlahov.sleepcalc.dto.WeeklyReportDTO;
import org.svlahov.sleepcalc.entity.SleepData;
import org.svlahov.sleepcalc.entity.User;
import org.svlahov.sleepcalc.repository.SleepDataRepository;
import org.svlahov.sleepcalc.repository.UserRepository;

import java.math.BigDecimal;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private SleepDataRepository sleepDataRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @Mock
    private UserDetails userDetails;

    private ReportServiceImpl reportService;

    @BeforeEach
    void setUp() {
        reportService = new ReportServiceImpl(sleepDataRepository, userRepository);

        SecurityContextHolder.setContext(securityContext);
    }

    private void mockUser() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(new User("testuser", "password")));
    }

    @Test
    void getWeeklyReport_shouldReturnCorrectData() {
        mockUser();
        LocalDate date = LocalDate.of(2023, 10, 27); // A Friday

        // Mock current week data
        SleepData d1 = new SleepData(new User("testuser", "pw"), LocalDate.of(2023, 10, 23), new BigDecimal("8.0"),
                BigDecimal.ZERO, BigDecimal.ZERO);
        when(sleepDataRepository.findByUser_UsernameAndSleepDateBetween(eq("testuser"), any(LocalDate.class),
                any(LocalDate.class)))
                .thenReturn(List.of(d1)); // Simplified for test

        WeeklyReportDTO report = reportService.getWeeklyReport(date);

        assertNotNull(report);
        assertEquals(1, report.dailyItems().stream().filter(i -> i.hoursSlept() > 0).count());
        // 8 hours slept vs 7.5 target = +0.5 surplus
        assertEquals(0.5, report.netSleepSurplus(), 0.01);
    }

    @Test
    void getMonthlyReport_shouldReturnCorrectData() {
        mockUser();
        LocalDate date = LocalDate.of(2023, 10, 27);

        // Mock data for the period
        SleepData d1 = new SleepData(new User("testuser", "pw"), LocalDate.of(2023, 10, 23), new BigDecimal("8.0"),
                BigDecimal.ZERO, BigDecimal.ZERO);
        when(sleepDataRepository.findByUser_UsernameAndSleepDateBetween(eq("testuser"), any(LocalDate.class),
                any(LocalDate.class)))
                .thenReturn(List.of(d1));

        MonthlyReportDTO report = reportService.getMonthlyReport(date);

        assertNotNull(report);
        assertEquals(4, report.weeklyItems().size());
    }

    @Test
    void exportReport_shouldReturnCsvString() {
        mockUser();
        LocalDate from = LocalDate.of(2023, 10, 1);
        LocalDate to = LocalDate.of(2023, 10, 31);

        SleepData d1 = new SleepData(new User("testuser", "pw"), LocalDate.of(2023, 10, 23), new BigDecimal("8.0"),
                BigDecimal.ZERO, BigDecimal.ZERO);
        when(sleepDataRepository.findByUser_UsernameAndSleepDateBetween("testuser", from, to))
                .thenReturn(List.of(d1));

        String csv = reportService.exportReport(from, to);

        assertNotNull(csv);
        assertTrue(csv.contains("Date,Hours Slept"));
        assertTrue(csv.contains("2023-10-23,8.0"));
    }
}
